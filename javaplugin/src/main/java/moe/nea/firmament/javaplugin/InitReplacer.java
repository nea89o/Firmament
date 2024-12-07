package moe.nea.firmament.javaplugin;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

public class InitReplacer extends TreeScanner<Void, Void> {
    private final MappingTree mappingTree;
    private final TreeMaker treeMaker;
    private final Names names;
    private final IntermediaryNameResolutionTask plugin;
    private Symbol.ClassSymbol classTree;
    private CompilationUnitTree compilationUnitTree;

    public InitReplacer(MappingTree mappingTree, IntermediaryNameResolutionTask plugin) {
        this.mappingTree = mappingTree;
        this.treeMaker = plugin.treeMaker;
        this.names = plugin.names;
        this.plugin = plugin;
    }

    @Override
    public Void visitClass(ClassTree node, Void unused) {
        this.classTree = plugin.utils.getSymbol(node);
        return super.visitClass(node, unused);
    }

    @Override
    public Void visitCompilationUnit(CompilationUnitTree node, Void unused) {
        this.compilationUnitTree = node;
        return super.visitCompilationUnit(node, unused);
    }

    @Override
    public Void visitVariable(VariableTree node, Void unused) {
        var annotation = node
            .getModifiers().getAnnotations()
            .stream()
            .filter(it -> it.getAnnotationType().toString().equals("IntermediaryName")) // Crazy type-safety!
            .findAny();
        if (annotation.isEmpty())
            return super.visitVariable(node, unused);
        var jcAnnotation = (JCTree.JCAnnotation) annotation.get();
        var jcNode = (JCTree.JCVariableDecl) node;
        if (node.getInitializer() != null) {
            plugin.utils.reportError(
                compilationUnitTree.getSourceFile(),
                jcNode.getInitializer(),
                "Providing an initializer for a variable is illegal for @IntermediaryName annotated fields"
            );
            return super.visitVariable(node, unused);
        }
        var target = plugin.utils.getAnnotationValue(jcAnnotation, "value");
        var targetClass = plugin.utils.resolveClassLiteralExpression(target).tsym.flatName().toString();
        var intermediaryClass = mappingTree.resolveClassToIntermediary(targetClass);
		if (intermediaryClass == null){
			plugin.utils.reportError(
				compilationUnitTree.getSourceFile(),
				jcNode.init,
				"Unknown class name " + targetClass
			);
			return super.visitVariable(node, unused);
		}
        var remapper = treeMaker.Select(treeMaker.This(classTree.type), names.fromString("remapper"));
        var remappingCall = treeMaker.Apply(
            List.nil(),
            treeMaker.Select(remapper, names.fromString("mapClassName")),
            List.of(treeMaker.Literal("intermediary"),
                    treeMaker.Literal(intermediaryClass)));
        jcNode.init = remappingCall;
        jcNode.mods.annotations = List.filter(jcNode.mods.annotations, jcAnnotation);
        return super.visitVariable(node, unused);
    }

}
