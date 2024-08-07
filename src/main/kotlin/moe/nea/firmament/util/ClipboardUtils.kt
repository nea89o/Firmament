

package moe.nea.firmament.util

import moe.nea.firmament.Firmament

object ClipboardUtils {
    fun setTextContent(string: String) {
        try {
            MC.keyboard.clipboard = string.ifEmpty { " " }
        } catch (e: Exception) {
            Firmament.logger.error("Could not write clipboard", e)
        }
    }

    fun getTextContents(): String {
        try {
            return MC.keyboard.clipboard ?: ""
        } catch (e: Exception) {
            Firmament.logger.error("Could not read clipboard", e)
            return ""
        }
    }
}
