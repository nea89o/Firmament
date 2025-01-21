package moe.nea.firmament.util.mc

import net.minecraft.util.Identifier

interface IntrospectableItemModelManager {
	fun hasModel_firmament(identifier: Identifier): Boolean
}
