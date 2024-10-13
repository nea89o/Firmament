

package moe.nea.firmament.util

import java.util.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import moe.nea.firmament.Firmament

object TemplateUtil {

    @JvmStatic
    fun getTemplatePrefix(data: String): String? {
        val decoded = maybeFromBase64Encoded(data) ?: return null
        return decoded.replaceAfter("/", "", "").ifBlank { null }
    }

    @JvmStatic
    fun intoBase64Encoded(raw: String): String {
        return Base64.getEncoder().encodeToString(raw.encodeToByteArray())
    }

    private val base64Alphabet = charArrayOf(
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/', '='
    )

    @JvmStatic
    fun maybeFromBase64Encoded(raw: String): String? {
        val raw = raw.trim()
        if (raw.any { it !in base64Alphabet }) {
            return null
        }
        return try {
            Base64.getDecoder().decode(raw).decodeToString()
        } catch (ex: Exception) {
            null
        }
    }


    /**
     * Returns a base64 encoded string, truncated such that for all `x`, `x.startsWith(prefix)` implies
     * `base64Encoded(x).startsWith(getPrefixComparisonSafeBase64Encoding(prefix))`
     * (however, the inverse may not always be true).
     */
    @JvmStatic
    fun getPrefixComparisonSafeBase64Encoding(prefix: String): String {
        val rawEncoded =
            Base64.getEncoder().encodeToString(prefix.encodeToByteArray())
                .replace("=", "")
        return rawEncoded.substring(0, rawEncoded.length - rawEncoded.length % 4)
    }

    inline fun <reified T> encodeTemplate(sharePrefix: String, data: T): String =
        encodeTemplate(sharePrefix, data, serializer())

    fun <T> encodeTemplate(sharePrefix: String, data: T, serializer: SerializationStrategy<T>): String {
        require(sharePrefix.endsWith("/"))
        return intoBase64Encoded(sharePrefix + Firmament.tightJson.encodeToString(serializer, data))
    }

    inline fun <reified T : Any> maybeDecodeTemplate(sharePrefix: String, data: String): T? =
        maybeDecodeTemplate(sharePrefix, data, serializer())

    fun <T : Any> maybeDecodeTemplate(sharePrefix: String, data: String, serializer: DeserializationStrategy<T>): T? {
        require(sharePrefix.endsWith("/"))
        val data = data.trim()
        if (!data.startsWith(getPrefixComparisonSafeBase64Encoding(sharePrefix)))
            return null
        val decoded = maybeFromBase64Encoded(data) ?: return null
        if (!decoded.startsWith(sharePrefix))
            return null
        return try {
            Firmament.json.decodeFromString<T>(serializer, decoded.substring(sharePrefix.length))
        } catch (e: Exception) {
            null
        }
    }

}
