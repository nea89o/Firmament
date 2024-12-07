package moe.nea.firmament.javaplugin;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

import javax.tools.JavaFileObject;

public class IntermediaryMethodReplacer extends TreeScanner<Void, Void> {
    private final MappingTree mappings;
    private final IntermediaryNameResolutionTask plugin;
    private JavaFileObject sourceFile;
    private CompilationUnitTree compilationUnit;

    public IntermediaryMethodReplacer(MappingTree mappings, IntermediaryNameResolutionTask plugin) {
        this.mappings = mappings;
        this.plugin = plugin;
    }


    @Override
    public Void visitCompilationUnit(CompilationUnitTree node, Void unused) {
        sourceFile = node.getSourceFile();
        compilationUnit = node;
        return super.visitCompilationUnit(node, unused);
    }

    public void replaceMethodName(JCTree.JCMethodInvocation node) {
        var select = node.getMethodSelect();
        if (!(select instanceof JCTree.JCFieldAccess fieldAccess)) return;
        if (!fieldAccess.name.contentEquals("methodName")) return;
        if (!(node.args.head instanceof JCTree.JCMemberReference methodReference)) {
            plugin.utils.reportError(sourceFile, node, "Please provide a Class::method reference directly (and nothing else)");
            return;
        }
        var clearName = methodReference.name.toString();
        var classRef = methodReference.expr;
        var type = plugin.utils.resolveClassName(classRef, compilationUnit);
        var intermediaryName = mappings.resolveMethodToIntermediary(
            type.tsym.flatName().toString(),
            clearName
        );
        fieldAccess.name = plugin.names.fromString("id");
        node.args = List.of(plugin.treeMaker.Literal(intermediaryName));
    }

    public void replaceClassName(JCTree.JCMethodInvocation node) {
        var select = node.getMethodSelect();
        if (!(select instanceof JCTree.JCFieldAccess fieldAccess)) return;
        if (!fieldAccess.name.contentEquals("className")) return;
        if (node.getTypeArguments().size() != 1) {
            plugin.utils.reportError(sourceFile, node, "You need to explicitly provide the class you want the intermediary name for");
            return;
        }
        var head = node.typeargs.head;
        var resolved = plugin.utils.resolveClassName(head, compilationUnit);
		var sourceName = resolved.tsym.flatName().toString();
        var mappedName = mappings.resolveClassToIntermediary(sourceName);
		if (mappedName == null) {
			plugin.utils.reportError(sourceFile, node, "Unknown class name " + sourceName);
			return;
		}
        fieldAccess.name = plugin.names.fromString("id");
        node.typeargs = List.nil();
        node.args = List.of(plugin.treeMaker.Literal(mappedName));
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void unused) {
        replaceClassName((JCTree.JCMethodInvocation) node);
        replaceMethodName((JCTree.JCMethodInvocation) node);
        return super.visitMethodInvocation(node, unused);
    }
}
