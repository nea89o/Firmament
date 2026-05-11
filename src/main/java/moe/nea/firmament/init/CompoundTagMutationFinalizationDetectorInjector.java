package moe.nea.firmament.init;

import moe.nea.firmament.util.ErrorUtil;
import moe.nea.firmament.util.mc.CompoundMutationChecker;
import net.minecraft.nbt.CompoundTag;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class CompoundTagMutationFinalizationDetectorInjector extends RiserUtils {
	Intermediary.InterClass CompoundTag = Intermediary.<CompoundTag>intermediaryClass();
	Intermediary.InterClass CompoundMutationChecker = Intermediary.ofClass(CompoundMutationChecker.class);
	Intermediary.InterMethod checkForMutations = Intermediary.ofMethod(
		"internalCheckForMutation",
		CompoundMutationChecker.intermediary().getClassName(),
		Intermediary.ofClass(Void.TYPE),
		CompoundTag,
		CompoundTag
	);
	Intermediary.InterClass Object = Intermediary.ofClass(Object.class);
	public static final String MUTATION_FIELD_NAME = "_firm_mutation_check";
	public static Type FINALIZE_DESC = Type.getMethodType(Type.VOID_TYPE);
	public static String FINALIZE_NAME = "finalize";

	@Override
	public void addTinkerers() {
		if (!ErrorUtil.aggressiveErrors)
			return;

		addTransformation(CompoundTag, this::injectFinalizationLogic, false);
	}

	private void injectFinalizationLogic(ClassNode classNode) {
		classNode.fields.add(
			new FieldNode(
				Opcodes.ACC_PUBLIC,
				MUTATION_FIELD_NAME,
				CompoundTag.mapped().getDescriptor(),
				null,
				null
			)
		);

		classNode.methods.removeIf(it -> it.name.equals(FINALIZE_NAME));
		classNode.methods.add(constructFinalizationMethod());
	}

	private MethodNode constructFinalizationMethod() {
		var node = new MethodNode(
			Opcodes.ACC_PUBLIC,
			FINALIZE_NAME,
			FINALIZE_DESC.getDescriptor(),
			null,
			null
		);
		var insns = new InsnList();
		insns.add(new IntInsnNode(Opcodes.ALOAD, 0));
		insns.add(new FieldInsnNode(
			Opcodes.GETFIELD,
			CompoundTag.mapped().getInternalName(),
			MUTATION_FIELD_NAME,
			CompoundTag.mapped().getDescriptor()
		));
		var sup = new LabelNode();
		insns.add(new JumpInsnNode(
			Opcodes.IFNULL,
			sup
		));
		insns.add(new IntInsnNode(Opcodes.ALOAD, 0));
		insns.add(new IntInsnNode(Opcodes.ALOAD, 0));
		insns.add(new FieldInsnNode(
			Opcodes.GETFIELD,
			CompoundTag.mapped().getInternalName(),
			MUTATION_FIELD_NAME,
			CompoundTag.mapped().getDescriptor()
		));
		insns.add(new MethodInsnNode(
			Opcodes.INVOKESTATIC,
			CompoundMutationChecker.mapped().getInternalName(),
			checkForMutations.mapped(),
			checkForMutations.mappedDesc().getDescriptor(),
			false
		));

		insns.add(sup);
		insns.add(new IntInsnNode(Opcodes.ALOAD, 0));
		insns.add(new MethodInsnNode(
			Opcodes.INVOKESPECIAL,
			Object.mapped().getInternalName(),
			FINALIZE_NAME,
			FINALIZE_DESC.getDescriptor(),
			false
		));
		insns.add(new InsnNode(Opcodes.RETURN));
		node.instructions = insns;
		return node;
	}
}
