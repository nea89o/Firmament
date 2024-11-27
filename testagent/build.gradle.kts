plugins {
	java
	alias(libs.plugins.shadow)
}
dependencies {
	implementation(libs.asm)
}
tasks.withType<Jar> {
	val agentMain = "moe.nea.firmament.testagent.AgentMain"
	manifest.attributes(
		"Agent-Class" to agentMain,
		"Premain-Class" to agentMain,
	)
}
