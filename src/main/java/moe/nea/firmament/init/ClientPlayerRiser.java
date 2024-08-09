package moe.nea.firmament.init;

import me.shedaniel.mm.api.ClassTinkerers;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.lang.reflect.Modifier;
import java.util.Objects;

public class ClientPlayerRiser extends RiserUtils {
    @IntermediaryName(net.minecraft.entity.player.PlayerEntity.class)
    String PlayerEntity;
    @IntermediaryName(net.minecraft.world.World.class)
    String World;
    String GameProfile = "com.mojang.authlib.GameProfile";
    @IntermediaryName(net.minecraft.util.math.BlockPos.class)
    String BlockPos;
    @IntermediaryName(net.minecraft.client.network.AbstractClientPlayerEntity.class)
    String AbstractClientPlayerEntity;
    String GuiPlayer = "moe.nea.firmament.gui.entity.GuiPlayer";
    // World world, BlockPos pos, float yaw, GameProfile gameProfile
    Type constructorDescriptor = Type.getMethodType(Type.VOID_TYPE, getTypeForClassName(World), getTypeForClassName(BlockPos), Type.FLOAT_TYPE, getTypeForClassName(GameProfile));


    private void mapClassNode(ClassNode classNode, Type superClass) {
        for (MethodNode method : classNode.methods) {
            if (Objects.equals(method.name, "<init>") && Type.getMethodType(method.desc).equals(constructorDescriptor)) {
                modifyConstructor(method, superClass);
                return;
            }
        }
        var node = new MethodNode(Opcodes.ASM9, "<init>", constructorDescriptor.getDescriptor(), null, null);
        classNode.methods.add(node);
        modifyConstructor(node, superClass);
    }


    private void modifyConstructor(MethodNode method, Type superClass) {
        method.access = (method.access | Modifier.PUBLIC) & ~Modifier.PRIVATE & ~Modifier.PROTECTED;
        if (method.instructions.size() != 0) return; // Some other mod has already made a constructor here

        // World world, BlockPos pos, float yaw, GameProfile gameProfile
        // ALOAD this
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));

        // ALOAD World
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));

        // ALOAD BlockPos
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));

        // ALOAD yaw
        method.instructions.add(new VarInsnNode(Opcodes.FLOAD, 3));

        // ALOAD gameProfile
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 4));

        // Call super
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, superClass.getInternalName(), "<init>", constructorDescriptor.getDescriptor(), false));

        // Return
        method.instructions.add(new InsnNode(Opcodes.RETURN));
    }

    @Override
    public void addTinkerers() {
        ClassTinkerers.addTransformation(AbstractClientPlayerEntity, it -> mapClassNode(it, getTypeForClassName(PlayerEntity)));
        ClassTinkerers.addTransformation(GuiPlayer, it -> mapClassNode(it, getTypeForClassName(AbstractClientPlayerEntity)));
    }
}
