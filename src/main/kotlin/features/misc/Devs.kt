package moe.nea.firmament.features.misc

import java.util.UUID

object Devs {
	data class Dev(
		val uuids: List<UUID>,
	) {
		constructor(vararg uuid: UUID) : this(uuid.toList())
		constructor(vararg uuid: String) : this(uuid.map { UUID.fromString(it) })
	}

	val nea = Dev("d3cb85e2-3075-48a1-b213-a9bfb62360c1", "842204e6-6880-487b-ae5a-0595394f9948")
	val kath = Dev("add71246-c46e-455c-8345-c129ea6f146c", "b491990d-53fd-4c5f-a61e-19d58cc7eddf")
	val jani = Dev("8a9f1841-48e9-48ed-b14f-76a124e6c9df")
}
