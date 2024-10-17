
package moe.nea.firmament.init;

import me.shedaniel.mm.api.ClassTinkerers;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
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
import java.util.function.Consumer;

public class HandledScreenRiser extends RiserUtils {
	@IntermediaryName(net.minecraft.client.gui.screen.Screen.class)
	String Screen;
	@IntermediaryName(net.minecraft.client.gui.screen.ingame.HandledScreen.class)
	String HandledScreen;
	Type mouseScrolledDesc = Type.getMethodType(Type.BOOLEAN_TYPE, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE, Type.DOUBLE_TYPE);
	String mouseScrolled = remapper.mapMethodName("intermediary", "net.minecraft.class_364", "method_25401",
	                                              mouseScrolledDesc.getDescriptor());
	// boolean keyReleased(int keyCode, int scanCode, int modifiers)
	Type keyReleasedDesc = Type.getMethodType(Type.BOOLEAN_TYPE, Type.INT_TYPE, Type.INT_TYPE, Type.INT_TYPE);
	String keyReleased = remapper.mapMethodName("intermediary", Intermediary.<Element>className(),
	                                            Intermediary.methodName(Element::keyReleased),
	                                            keyReleasedDesc.getDescriptor());


	@Override
	public void addTinkerers() {
		ClassTinkerers.addTransformation(HandledScreen, this::addMouseScroll, true);
		ClassTinkerers.addTransformation(HandledScreen, this::addKeyReleased, true);
	}

	/**
	 * Insert a handler that roughly inserts the following code at the beginning of the instruction list:
	 * <code><pre>
	 * if (insertInvoke(insertLoads)) return true
	 * </pre></code>
	 *
	 * @param node         The method node to prepend the instructions to
	 * @param insertLoads  insert all the loads, including the {@code this} parameter
	 * @param insertInvoke insert the invokevirtual/invokestatic call
	 */
	void insertTrueHandler(MethodNode node,
	                       Consumer<InsnList> insertLoads,
	                       Consumer<InsnList> insertInvoke) {

		var insns = new InsnList();
		insertLoads.accept(insns);
		insertInvoke.accept(insns);
		// Create jump target (but not insert it yet)
		var jumpIfFalse = new LabelNode();
		// IFEQ (if returned boolean == 0), jump to jumpIfFalse
		insns.add(new JumpInsnNode(Opcodes.IFEQ, jumpIfFalse));
		// LDC 1 (as int, which is what booleans are at runtime)
		insns.add(new LdcInsnNode(1));
		// IRETURN return int on stack (booleans are int at runtime)
		insns.add(new InsnNode(Opcodes.IRETURN));
		insns.add(jumpIfFalse);
		node.instructions.insert(insns);
	}

	void addKeyReleased(ClassNode classNode) {
		var keyReleasedNode = findMethod(classNode, keyReleased, keyReleasedDesc);
		if (keyReleasedNode == null) {
			keyReleasedNode = new MethodNode(
				Modifier.PUBLIC,
				keyReleased,
				keyReleasedDesc.getDescriptor(),
				null,
				new String[0]
			);
			var insns = keyReleasedNode.instructions;
			// ALOAD 0, load this
			insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			// ILOAD 1-3, load args
			insns.add(new VarInsnNode(Opcodes.ILOAD, 1));
			insns.add(new VarInsnNode(Opcodes.ILOAD, 2));
			insns.add(new VarInsnNode(Opcodes.ILOAD, 3));
			// INVOKESPECIAL call super method
			insns.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, getTypeForClassName(Screen).getInternalName(),
			                             keyReleased, keyReleasedDesc.getDescriptor()));
			// IRETURN return int on stack (booleans are int at runtime)
			insns.add(new InsnNode(Opcodes.IRETURN));
			classNode.methods.add(keyReleasedNode);
		}
		insertTrueHandler(keyReleasedNode, insns -> {
			// ALOAD 0, load this
			insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			// ILOAD 1-3, load args
			insns.add(new VarInsnNode(Opcodes.ILOAD, 1));
			insns.add(new VarInsnNode(Opcodes.ILOAD, 2));
			insns.add(new VarInsnNode(Opcodes.ILOAD, 3));
		}, insns -> {
			// INVOKEVIRTUAL call custom handler
			insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
			                             getTypeForClassName(HandledScreen).getInternalName(),
			                             "keyReleased_firmament",
			                             keyReleasedDesc.getDescriptor()));
		});
	}

	void addMouseScroll(ClassNode classNode) {
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

		insertTrueHandler(mouseScrolledNode, insns -> {
			// ALOAD 0, load this
			insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
			// DLOAD 1-4, load the 4 argument doubles. Note that since doubles are two entries wide we skip 2 each time.
			insns.add(new VarInsnNode(Opcodes.DLOAD, 1));
			insns.add(new VarInsnNode(Opcodes.DLOAD, 3));
			insns.add(new VarInsnNode(Opcodes.DLOAD, 5));
			insns.add(new VarInsnNode(Opcodes.DLOAD, 7));
		}, insns -> {
			// INVOKEVIRTUAL call custom handler
			insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
			                             getTypeForClassName(HandledScreen).getInternalName(),
			                             "mouseScrolled_firmament",
			                             mouseScrolledDesc.getDescriptor()));

		});
	}

}
