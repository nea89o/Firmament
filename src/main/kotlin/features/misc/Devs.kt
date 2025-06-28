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

	object FurfSky {
		val smolegit = Dev("02b38b96-eb19-405a-b319-d6bc00b26ab3")
		val itsCen = Dev("ada70b5a-ac37-49d2-b18c-1351672f8051")
		val webster = Dev("02166f1b-9e8d-4e48-9e18-ea7a4499492d")
		val vrachel = Dev("22e98637-ba97-4b6b-a84f-fb57a461ce43")
		val cunuduh = Dev("2a15e3b3-c46e-4718-b907-166e1ab2efdc")
		val eiiies = Dev("2ae162f2-81a7-4f91-a4b2-104e78a0a7e1")
		val june = Dev("2584a4e3-f917-4493-8ced-618391f3b44f")
		val denasu = Dev("313cbd25-8ade-4e41-845c-5cab555a30c9")
		val libyKiwii = Dev("4265c52e-bd6f-4d3c-9cf6-bdfc8fb58023")
		val madeleaan = Dev("bcb119a3-6000-4324-bda1-744f00c44b31")
		val turtleSP = Dev("f1ca1934-a582-4723-8283-89921d008657")
		val papayamm = Dev("ae0eea30-f6a2-40fe-ac17-9c80b3423409")
		val persuasiveViksy = Dev("ba7ac144-28e0-4f55-a108-1a72fe744c9e")
		val all = listOf(
			smolegit, itsCen, webster, vrachel, cunuduh, eiiies,
			june, denasu, libyKiwii, madeleaan, turtleSP, papayamm,
			persuasiveViksy
		)
	}

}
