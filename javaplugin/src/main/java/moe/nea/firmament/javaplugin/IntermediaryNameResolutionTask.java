package moe.nea.firmament.javaplugin;

import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import net.fabricmc.stitch.commands.tinyv2.TinyV2Reader;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class IntermediaryNameResolutionTask implements TaskListener {
    TreeMaker treeMaker;
    Names names;
    MappingTree mappings;
    Utils utils;

    public IntermediaryNameResolutionTask(IntermediaryNameResolutionPlugin intermediaryNameResolutionPlugin, JavacTask task, Map<String, String> argMap) {
        var context = ((BasicJavacTask) task).getContext();
        var mappingFile = new File(argMap.get("mappingFile"));
        System.err.println("Loading mappings from " + mappingFile);
        try {
            var tinyV2File = TinyV2Reader.read(mappingFile.toPath());
            mappings = new MappingTree(tinyV2File, argMap.get("sourceNs"), argMap.getOrDefault("targetNs", "intermediary"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        treeMaker = TreeMaker.instance(context);
        names = Names.instance(context);
        utils = Utils.instance(context);
    }

    @Override
    public void finished(TaskEvent e) {
        if (e.getKind() != TaskEvent.Kind.ENTER) return;
        if (e.getCompilationUnit() == null || e.getSourceFile() == null) return;
        e.getCompilationUnit().accept(new InitReplacer(mappings, this), null);
        e.getCompilationUnit().accept(new IntermediaryMethodReplacer(mappings, this), null);
    }

}
