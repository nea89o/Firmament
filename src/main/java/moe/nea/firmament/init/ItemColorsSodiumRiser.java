package moe.nea.firmament.init;

import me.shedaniel.mm.api.ClassTinkerers;
import moe.nea.firmament.util.ErrorUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class ItemColorsSodiumRiser extends RiserUtils {
	@IntermediaryName(ItemColors.class)
	String ItemColors;
	@IntermediaryName(ItemColorProvider.class)
	String ItemColorProvider;
	@IntermediaryName(ItemStack.class)
	String ItemStack;
	String getColorProvider = "sodium$getColorProvider";
	Type getColorProviderDesc = Type.getMethodType(getTypeForClassName(ItemColorProvider),
	                                               getTypeForClassName(ItemStack));

	@Override
	public void addTinkerers() {
		ClassTinkerers.addTransformation(ItemColors, this::addSodiumOverride, true);
	}

	private void addSodiumOverride(ClassNode classNode) {
		var node = findMethod(classNode, getColorProvider, getColorProviderDesc);
		if (node == null) {
			if (!FabricLoader.getInstance().isModLoaded("sodium"))
				ErrorUtil.INSTANCE.softError("Sodium is present, but sodium color override could not be injected.");
			return;
		}
		var p = node.instructions.getFirst();
		while (p != null) {
			if (p.getOpcode() == Opcodes.ARETURN) {
				node.instructions.insertBefore(
					p,
					mkOverrideSodiumCall()
				);
			}
			p = p.getNext();
		}
	}

	private InsnList mkOverrideSodiumCall() {
		var insnList = new InsnList();
		insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
		insnList.add(new InsnNode(Opcodes.SWAP));
		insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
		                                getTypeForClassName(ItemColors).getInternalName(),
		                                "overrideSodium_firmament",
		                                Type.getMethodType(getTypeForClassName(ItemColorProvider),
		                                                   getTypeForClassName(ItemColorProvider)).getDescriptor(),
		                                false));
		return insnList;
	}
}
