package moe.nea.firmament.testagent;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class ProtectedToPublicClassTransformer implements ClassFileTransformer {
	public ProtectedToPublicClassTransformer(Instrumentation inst) {
	}

	@Override
	public byte[] transform(ClassLoader loader,
	                        String className,
	                        Class<?> classBeingRedefined,
	                        ProtectionDomain protectionDomain,
	                        byte[] classfileBuffer)
		throws IllegalClassFormatException {
		if (!className.startsWith("net/minecraft/")) return classfileBuffer;
		if (classfileBuffer == null) return null;
		var reader = new ClassReader(classfileBuffer);
		var writer = new ClassWriter(0);
		var transformer = new ProtectedToPublicClassRewriter(writer);
		reader.accept(transformer, 0);
		return writer.toByteArray();
	}
}
