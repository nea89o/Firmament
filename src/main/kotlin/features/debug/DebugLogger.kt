package moe.nea.firmament.features.debug

import kotlinx.serialization.serializer
import net.minecraft.text.Text
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.collections.InstanceList
import moe.nea.firmament.util.data.DataHolder

class DebugLogger(val tag: String) {
	companion object {
		val allInstances = InstanceList<DebugLogger>("DebugLogger")
	}
	object EnabledLogs : DataHolder<MutableSet<String>>(serializer(), "DebugLogs", ::mutableSetOf)

	init {
		allInstances.add(this)
	}

	fun isEnabled() = DeveloperFeatures.isEnabled && EnabledLogs.data.contains(tag)
	fun log(text: () -> String) {
		if (!isEnabled()) return
		MC.sendChat(Text.literal(text()))
	}
}
