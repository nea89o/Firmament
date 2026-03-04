package moe.nea.firmament.init;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
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

	Intermediary.InterClass SectionBuilder = Intermediary.<SectionCompiler>intermediaryClass();
	Intermediary.InterClass BlockPos = Intermediary.<BlockPos>intermediaryClass();
	Intermediary.InterClass BlockRenderManager = Intermediary.<BlockRenderDispatcher>intermediaryClass();
	Intermediary.InterClass BlockState = Intermediary.<BlockState>intermediaryClass();
	Intermediary.InterClass BlockStateModel = Intermediary.<BlockStateModel>intermediaryClass();
	// Use a class literal so ProGuard tracks and renames BlockRenderHooks correctly,
	// rather than a hardcoded string that ProGuard cannot update.
	Type BlockRenderHooksType = Type.getType(BlockRenderHooks.class);

	Intermediary.InterMethod getModel =
		Intermediary.intermediaryMethod(
			net.minecraft.client.renderer.block.BlockRenderDispatcher::getBlockModel,
			BlockStateModel,
			BlockState
		);

	@Override
	public void addTinkerers() {
		if (FabricLoader.getInstance().isModLoaded("fabric-renderer-indigo"))
			addTransformation(SectionBuilder, this::handle, true);
	}

	private void handle(ClassNode classNode) {
		System.out.println("AVAST! "+ getModel);
		for (MethodNode method : classNode.methods) {
			if ((method.name.endsWith("$fabric-renderer-indigo$hookBuildRenderBlock")
			     || method.name.endsWith("$fabric-renderer-indigo$hookChunkBuildTessellate")) &&
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
			if (Type.getType(localVariable.desc).equals(BlockPos.mapped())) {
				blockPosVar = localVariable;
			}
			if (Type.getType(localVariable.desc).equals(BlockState.mapped())) {
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
			if (!(methodInsn.name.equals(getModel.mapped()) &&
				Type.getObjectType(methodInsn.owner).equals(BlockRenderManager.mapped())))
				continue;
			method.instructions.insertBefore(
				methodInsn,
				new MethodInsnNode(
					Opcodes.INVOKESTATIC,
					BlockRenderHooksType.getInternalName(),
					"enterFallbackCall",
					Type.getMethodDescriptor(Type.VOID_TYPE)
				));

			var insnList = new InsnList();
			insnList.add(new MethodInsnNode(
				Opcodes.INVOKESTATIC,
				BlockRenderHooksType.getInternalName(),
				"exitFallbackCall",
				Type.getMethodDescriptor(Type.VOID_TYPE)
			));
			insnList.add(new VarInsnNode(Opcodes.ALOAD, blockPosVar.index));
			insnList.add(new VarInsnNode(Opcodes.ALOAD, blockStateVar.index));
			insnList.add(new MethodInsnNode(
				Opcodes.INVOKESTATIC,
				BlockRenderHooksType.getInternalName(),
				"patchIndigo",
				Type.getMethodDescriptor(
					(BlockStateModel).mapped(),
					(BlockStateModel).mapped(),
					(BlockPos).mapped(),
					(BlockState).mapped()),
				false
			));
			method.instructions.insert(methodInsn, insnList);
		}
	}
}
