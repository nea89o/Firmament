package moe.nea.firmament.javaplugin;

import net.fabricmc.stitch.commands.tinyv2.TinyClass;
import net.fabricmc.stitch.commands.tinyv2.TinyFile;
import net.fabricmc.stitch.commands.tinyv2.TinyMethod;

import java.util.Map;
import java.util.stream.Collectors;

public class MappingTree {

    private final Map<String, TinyClass> classLookup;
    private final int targetIndex;
    private final int sourceIndex;

    public MappingTree(TinyFile tinyV2File, String sourceNamespace, String targetNamespace) {
        sourceIndex = tinyV2File.getHeader().getNamespaces().indexOf(sourceNamespace);
        if (sourceIndex < 0)
            throw new RuntimeException("Could not find source namespace " + sourceNamespace + " in mappings file.");
        this.classLookup = tinyV2File
            .getClassEntries()
            .stream()
            .collect(Collectors.toMap(it -> it.getClassNames().get(sourceIndex), it -> it));
        targetIndex = tinyV2File.getHeader().getNamespaces().indexOf(targetNamespace);
        if (targetIndex < 0)
            throw new RuntimeException("Could not find target namespace " + targetNamespace + " in mappings file.");
    }

    public String resolveMethodToIntermediary(String className, String methodName) {
        var classData = classLookup.get(className.replace(".", "/"));
        TinyMethod candidate = null;
        for (TinyMethod method : classData.getMethods()) {
            if (method.getMethodNames().get(sourceIndex).equals(methodName)) {
                if (candidate != null) {
                    throw new RuntimeException("Found two candidates for method " + className + "." + methodName);
                }
                candidate = method;
            }
        }
        return candidate.getMethodNames().get(targetIndex);
    }

    public String resolveClassToIntermediary(String className) {
        return classLookup.get(className.replace(".", "/"))
                          .getClassNames().get(targetIndex)
                          .replace("/", ".");
    }
}
