
package moe.nea.firmament.features.debug

import net.minecraft.text.Text
import moe.nea.firmament.util.MC

class DebugLogger(val tag: String) {
    fun isEnabled() = DeveloperFeatures.isEnabled // TODO: allow filtering by tag
    fun log(text: () -> String) {
        if (!isEnabled()) return
        MC.sendChat(Text.literal(text()))
    }
}
