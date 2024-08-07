
package moe.nea.firmament.init;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public abstract class RiserUtils {
    protected Type getTypeForClassName(String className) {
        return Type.getObjectType(className.replace('.', '/'));
    }

    protected MappingResolver remapper = FabricLoader.getInstance().getMappingResolver();

    public abstract void addTinkerers();

    protected MethodNode findMethod(ClassNode classNode, String name, Type desc) {
        for (MethodNode method : classNode.methods) {
            if (method.name.equals(name) && desc.getDescriptor().equals(method.desc))
                return method;
        }
        return null;
    }

}
