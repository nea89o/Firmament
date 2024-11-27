package moe.nea.firmament.testagent;

import java.lang.instrument.Instrumentation;

public class AgentMain {

	public static void premain(
		String agentArgs, Instrumentation inst) {
		System.out.println("Pre-Main Firmament Test Agent");
		AgentMain.inject(inst);
	}

	public static void agentmain(
		String agentArgs, Instrumentation inst) {
		System.out.println("Injected Firmament Test Agent");
		AgentMain.inject(inst);
	}

	private static void inject(Instrumentation inst) {
		inst.addTransformer(new ProtectedToPublicClassTransformer(inst));	}
}
