

package moe.nea.firmament.events

import moe.nea.firmament.util.unformattedString
import net.minecraft.text.Text

/**
 * Allow modification of a chat message before it is sent off to the user. Intended for display purposes.
 */
data class ModifyChatEvent(val originalText: Text) : FirmamentEvent() {
    var unformattedString = originalText.unformattedString
        private set
    var replaceWith: Text = originalText
        set(value) {
            field = value
            unformattedString = value.unformattedString
        }

    companion object : FirmamentEventBus<ModifyChatEvent>()
}
