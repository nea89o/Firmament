package moe.nea.firmament.testagent;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ProtectedToPublicClassRewriter extends ClassVisitor {
	public ProtectedToPublicClassRewriter(ClassWriter writer) {
		super(Opcodes.ASM9, writer);
	}

	int makePublic(int flags) {
		if ((flags & Opcodes.ACC_PROTECTED) != 0)
			return (flags & ~Opcodes.ACC_PROTECTED) | Opcodes.ACC_PUBLIC;
		if ((flags & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE)) == 0)
			return flags | Opcodes.ACC_PUBLIC;
		return flags;
	}

	@Override
	public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
		return super.visitField(makePublic(access), name, descriptor, signature, value);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
		return super.visitMethod(makePublic(access), name, descriptor, signature, exceptions);
	}
}
