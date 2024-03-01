/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.init;

import me.shedaniel.mm.api.ClassTinkerers;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.Objects;

public class EarlyRiser implements Runnable {
    MappingResolver remapper = FabricLoader.getInstance().getMappingResolver();
    String PlayerEntity = remapper.mapClassName("intermediary", "net.minecraft.class_1657");
    String World = remapper.mapClassName("intermediary", "net.minecraft.class_1937");
    String GameProfile = "com.mojang.authlib.GameProfile";
    String BlockPos = remapper.mapClassName("intermediary", "net.minecraft.class_2338");
    String AbstractClientPlayerEntity = remapper.mapClassName("intermediary", "net.minecraft.class_742");
    String GuiPlayer = "moe.nea.firmament.gui.entity.GuiPlayer";
    // World world, BlockPos pos, float yaw, GameProfile gameProfile
    Type constructorDescriptor = Type.getMethodType(Type.VOID_TYPE, getTypeForClassName(World), getTypeForClassName(BlockPos), Type.FLOAT_TYPE, getTypeForClassName(GameProfile));

    @Override
    public void run() {
        ClassTinkerers.addTransformation(AbstractClientPlayerEntity, it -> mapClassNode(it, getTypeForClassName(PlayerEntity)));
        ClassTinkerers.addTransformation(GuiPlayer, it -> mapClassNode(it, getTypeForClassName(AbstractClientPlayerEntity)));
    }

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

    private Type getTypeForClassName(String className) {
        return Type.getObjectType(className.replace('.', '/'));
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
}
