
package moe.nea.firmament.init;

import me.shedaniel.mm.api.ClassTinkerers;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.lang.reflect.Modifier;

public class HandledScreenRiser extends RiserUtils {
    @IntermediaryName(net.minecraft.client.gui.screen.Screen.class)
    String Screen;
    @IntermediaryName(net.minecraft.client.gui.screen.ingame.HandledScreen.class)
    String HandledScreen;
    Type mouseScrolledDesc = Type.getMethodType(Type.BOOLEAN_TYPE, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE);
    String mouseScrolled = remapper.mapMethodName("intermediary", "net.minecraft.class_364", "method_25401",
                                                  mouseScrolledDesc.getDescriptor());

    @Override
    public void addTinkerers() {
        ClassTinkerers.addTransformation(HandledScreen, this::handle, true);
    }

    void handle(ClassNode classNode) {
        MethodNode mouseScrolledNode = findMethod(classNode, mouseScrolled, mouseScrolledDesc);
        if (mouseScrolledNode == null) {
            mouseScrolledNode = new MethodNode(
                Modifier.PUBLIC,
                mouseScrolled,
                mouseScrolledDesc.getDescriptor(),
                null,
                new String[0]
            );
            var insns = mouseScrolledNode.instructions;
            // ALOAD 0, load this
            insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
            // DLOAD 1-4, load the 4 argument doubles. Note that since doubles are two entries wide we skip 2 each time.
            insns.add(new VarInsnNode(Opcodes.DLOAD, 1));
            insns.add(new VarInsnNode(Opcodes.DLOAD, 3));
            insns.add(new VarInsnNode(Opcodes.DLOAD, 5));
            insns.add(new VarInsnNode(Opcodes.DLOAD, 7));
            // INVOKESPECIAL call super method
            insns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, getTypeForClassName(Screen).getInternalName(), mouseScrolled, mouseScrolledDesc.getDescriptor()));
            // IRETURN return int on stack (booleans are int at runtime)
            insns.add(new InsnNode(Opcodes.IRETURN));
            classNode.methods.add(mouseScrolledNode);
        }

        var insns = new InsnList();
        // ALOAD 0, load this
        insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
        // DLOAD 1-4, load the 4 argument doubles. Note that since doubles are two entries wide we skip 2 each time.
        insns.add(new VarInsnNode(Opcodes.DLOAD, 1));
        insns.add(new VarInsnNode(Opcodes.DLOAD, 3));
        insns.add(new VarInsnNode(Opcodes.DLOAD, 5));
        insns.add(new VarInsnNode(Opcodes.DLOAD, 7));
        // INVOKEVIRTUAL call custom handler
        insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                                     getTypeForClassName(HandledScreen).getInternalName(),
                                     "mouseScrolled_firmament",
                                     mouseScrolledDesc.getDescriptor()));
        // Create jump target (but not insert it yet)
        var jumpIfFalse = new LabelNode();
        // IFEQ (if returned boolean == 0), jump to jumpIfFalse
        insns.add(new JumpInsnNode(Opcodes.IFEQ, jumpIfFalse));
        // LDC 1 (as int, which is what booleans are at runtime)
        insns.add(new LdcInsnNode(1));
        // IRETURN return int on stack (booleans are int at runtime)
        insns.add(new InsnNode(Opcodes.IRETURN));
        insns.add(jumpIfFalse);
        mouseScrolledNode.instructions.insert(insns);
    }

}
