package moe.nea.firmament.javaplugin;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.JavacMessages;
import com.sun.tools.javac.util.Log;

import javax.tools.JavaFileObject;
import java.util.ListResourceBundle;

public class Utils {
    private static final Context.Key<Utils> KEY = new Context.Key<>();
    private final Log log;
    private final JCDiagnostic.Factory diagnostics;
    private final Types types;
    private final Attr attr;
    private final Enter enter;

    private Utils(Context context) {
        context.put(KEY, this);
        JavacMessages.instance(context).add(l -> new ListResourceBundle() {

            @Override
            protected Object[][] getContents() {
                return new Object[][]{
                    new Object[]{"compiler.err.firmament.generic", "{0}"}
                };
            }
        });
        log = Log.instance(context);
        diagnostics = JCDiagnostic.Factory.instance(context);
        types = Types.instance(context);
        attr = Attr.instance(context);
        enter = Enter.instance(context);
    }

    public static Utils instance(Context context) {
        var utils = context.get(KEY);
        if (utils == null) {
            utils = new Utils(context);
        }
        return utils;
    }

    public Type resolveClassName(ExpressionTree expression) {
        var tree = (JCTree) expression;
        return tree.type;
    }

    public Type resolveClassName(ExpressionTree tree, CompilationUnitTree unit) {
        return resolveClassName(tree, enter.getTopLevelEnv((JCTree.JCCompilationUnit) unit));
    }

    public Type resolveClassName(ExpressionTree tree, Env<AttrContext> env) {
        var t = resolveClassName(tree);
        if (t != null) return t;
        return attr.attribType((JCTree) tree, env);
    }

    public Symbol getSymbol(IdentifierTree tree) {
        return ((JCTree.JCIdent) tree).sym;
    }

    public Symbol.ClassSymbol getSymbol(ClassTree tree) {
        return ((JCTree.JCClassDecl) tree).sym;
    }

    public ExpressionTree getAnnotationValue(
        AnnotationTree tree,
        String name) {
        // TODO: strip parenthesis
        for (var argument : tree.getArguments()) {
            var assignment = (AssignmentTree) argument;
            if (((IdentifierTree) assignment.getVariable()).getName().toString().equals(name))
                return assignment.getExpression();
        }
        return null;
    }

    public Type.ClassType resolveClassLiteralExpression(ExpressionTree tree) {
        if (!(tree instanceof MemberSelectTree select))
            throw new RuntimeException("Cannot resolve non field access class literal: " + tree);
        if (!select.getIdentifier().toString().equals("class"))
            throw new RuntimeException("Class literal " + select + "accessed non .class attribute");

        return (Type.ClassType) resolveClassName(select.getExpression());
    }

    public void reportError(
        JavaFileObject file,
        Tree node,
        String message
    ) {
        var originalSource = log.useSource(file);
        var error = diagnostics.error(
            JCDiagnostic.DiagnosticFlag.API,
            log.currentSource(),
            node == null ? null : ((JCTree) node).pos(),
            "firmament.generic",
            message
        );
        log.report(error);
        log.useSource(originalSource);
    }
}
