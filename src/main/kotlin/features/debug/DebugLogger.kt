
package moe.nea.firmament.features.debug

import net.minecraft.text.Text
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.collections.InstanceList

class DebugLogger(val tag: String) {
    companion object {
        val allInstances = InstanceList<DebugLogger>("DebugLogger")
    }
    init {
    	allInstances.add(this)
    }
    fun isEnabled() = DeveloperFeatures.isEnabled // TODO: allow filtering by tag
    fun log(text: () -> String) {
        if (!isEnabled()) return
        MC.sendChat(Text.literal(text()))
    }
}
