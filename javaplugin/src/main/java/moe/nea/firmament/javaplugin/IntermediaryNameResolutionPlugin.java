package moe.nea.firmament.javaplugin;

import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;

import java.util.HashMap;
import java.util.Map;

public class IntermediaryNameResolutionPlugin implements Plugin {

    @Override
    public String getName() {
        return "IntermediaryNameReplacement";
    }

    @Override
    public void init(JavacTask task, String... args) {
        Map<String, String> argMap = new HashMap<>();
        for (String arg : args) {
            String[] parts = arg.split("=", 2);
            argMap.put(parts[0], parts.length == 2 ? parts[1] : "true");
        }
        task.addTaskListener(new IntermediaryNameResolutionTask(this, task, argMap));
    }
}
