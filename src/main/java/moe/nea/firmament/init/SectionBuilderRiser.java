package moe.nea.firmament.init;

import me.shedaniel.mm.api.ClassTinkerers;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.SectionBuilder;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.math.BlockPos;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class SectionBuilderRiser extends RiserUtils {

    @IntermediaryName(SectionBuilder.class)
    String SectionBuilder;
    @IntermediaryName(BlockPos.class)
    String BlockPos;
    @IntermediaryName(BlockRenderManager.class)
    String BlockRenderManager;
    @IntermediaryName(BlockState.class)
    String BlockState;
    @IntermediaryName(BakedModel.class)
    String BakedModel;
    String CustomBlockTextures = "moe.nea.firmament.features.texturepack.CustomBlockTextures";

    Type getModelDesc = Type.getMethodType(
        getTypeForClassName(BlockRenderManager),
        getTypeForClassName(BlockState)
    );
    String getModel = remapper.mapMethodName(
        "intermediary",
        Intermediary.<BlockRenderManager>className(),
        Intermediary.methodName(net.minecraft.client.render.block.BlockRenderManager::getModel),
        Type.getMethodDescriptor(
            getTypeForClassName(Intermediary.<BakedModel>className()),
            getTypeForClassName(Intermediary.<BlockState>className())
        )
    );

    @Override
    public void addTinkerers() {
        if (FabricLoader.getInstance().isModLoaded("fabric-renderer-indigo"))
            ClassTinkerers.addTransformation(SectionBuilder, this::handle, true);
    }

    private void handle(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            if (method.name.endsWith("$fabric-renderer-indigo$hookChunkBuildTessellate") &&
                method.name.startsWith("redirect$")) {
                handleIndigo(method);
                return;
            }
        }
        System.err.println("Could not inject indigo rendering hook. Is a custom renderer installed (e.g. sodium)?");
    }

    private void handleIndigo(MethodNode method) {
        LocalVariableNode blockPosVar = null, blockStateVar = null;
        for (LocalVariableNode localVariable : method.localVariables) {
            if (Type.getType(localVariable.desc).equals(getTypeForClassName(BlockPos))) {
                blockPosVar = localVariable;
            }
            if (Type.getType(localVariable.desc).equals(getTypeForClassName(BlockState))) {
                blockStateVar = localVariable;
            }
        }
        if (blockPosVar == null || blockStateVar == null) {
            System.err.println("Firmament could inject into indigo: missing either block pos or blockstate");
            return;
        }
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction.getOpcode() != Opcodes.INVOKEVIRTUAL) continue;
            var methodInsn = (MethodInsnNode) instruction;
            if (!(methodInsn.name.equals(getModel) && Type.getObjectType(methodInsn.owner).equals(getTypeForClassName(BlockRenderManager))))
                continue;
            method.instructions.insertBefore(
                methodInsn,
                new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    getTypeForClassName(CustomBlockTextures).getInternalName(),
                    "enterFallbackCall",
                    Type.getMethodDescriptor(Type.VOID_TYPE)
                ));

            var insnList = new InsnList();
            insnList.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                getTypeForClassName(CustomBlockTextures).getInternalName(),
                "exitFallbackCall",
                Type.getMethodDescriptor(Type.VOID_TYPE)
            ));
            insnList.add(new VarInsnNode(Opcodes.ALOAD, blockPosVar.index));
            insnList.add(new VarInsnNode(Opcodes.ALOAD, blockStateVar.index));
            insnList.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                getTypeForClassName(CustomBlockTextures).getInternalName(),
                "patchIndigo",
                Type.getMethodDescriptor(getTypeForClassName(BakedModel),
                                         getTypeForClassName(BakedModel),
                                         getTypeForClassName(BlockPos),
                                         getTypeForClassName(BlockState)),
                false
            ));
            method.instructions.insert(methodInsn, insnList);
        }
    }
}
