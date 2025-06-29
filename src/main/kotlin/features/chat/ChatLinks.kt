package moe.nea.firmament.features.chat

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import java.net.URI
import java.net.URL
import java.util.Collections
import java.util.concurrent.atomic.AtomicInteger
import moe.nea.jarvis.api.Point
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlin.math.min
import net.minecraft.client.gui.screen.ChatScreen
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.ModifyChatEvent
import moe.nea.firmament.events.ScreenRenderPostEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.render.drawTexture
import moe.nea.firmament.util.transformEachRecursively
import moe.nea.firmament.util.unformattedString

object ChatLinks : FirmamentFeature {
	override val identifier: String
		get() = "chat-links"

	object TConfig : ManagedConfig(identifier, Category.CHAT) {
		val enableLinks by toggle("links-enabled") { true }
		val imageEnabled by toggle("image-enabled") { true }
		val allowAllHosts by toggle("allow-all-hosts") { false }
		val allowedHosts by string("allowed-hosts") { "cdn.discordapp.com,media.discordapp.com,media.discordapp.net,i.imgur.com" }
		val actualAllowedHosts get() = allowedHosts.split(",").map { it.trim() }
		val position by position("position", 16 * 20, 9 * 20) { Point(0.0, 0.0) }
	}

	private fun isHostAllowed(host: String) =
		TConfig.allowAllHosts || TConfig.actualAllowedHosts.any { it.equals(host, ignoreCase = true) }

	private fun isUrlAllowed(url: String) = isHostAllowed(url.removePrefix("https://").substringBefore("/"))

	override val config get() = TConfig
	val urlRegex = "https://[^. ]+\\.[^ ]+(\\.?(\\s|$))".toRegex()
	val nextTexId = AtomicInteger(0)

	data class Image(
		val texture: Identifier,
		val width: Int,
		val height: Int,
	)

	val imageCache: MutableMap<String, Deferred<Image?>> =
		Collections.synchronizedMap(mutableMapOf<String, Deferred<Image?>>())

	private fun tryCacheUrl(url: String) {
		if (!isUrlAllowed(url)) {
			return
		}
		if (url in imageCache) {
			return
		}
		imageCache[url] = Firmament.coroutineScope.async {
			try {
				val response = Firmament.httpClient.get(URL(url))
				if (response.status.value == 200) {
					val inputStream = response.bodyAsChannel().toInputStream(Firmament.globalJob)
					val image = NativeImage.read(inputStream)
					val texId = Firmament.identifier("dynamic_image_preview${nextTexId.getAndIncrement()}")
					MC.textureManager.registerTexture(
						texId,
						NativeImageBackedTexture({ texId.path }, image)
					)
					Image(texId, image.width, image.height)
				} else
					null
			} catch (exc: Exception) {
				exc.printStackTrace()
				null
			}
		}
	}

	val imageExtensions = listOf("jpg", "png", "gif", "jpeg")
	fun isImageUrl(url: String): Boolean {
		return (url.substringAfterLast('.').lowercase() in imageExtensions)
	}

	@Subscribe
	@OptIn(ExperimentalCoroutinesApi::class)
	fun onRender(it: ScreenRenderPostEvent) {
		if (!TConfig.imageEnabled) return
		if (it.screen !is ChatScreen) return
		val hoveredComponent =
			MC.inGameHud.chatHud.getTextStyleAt(it.mouseX.toDouble(), it.mouseY.toDouble()) ?: return
		val hoverEvent = hoveredComponent.hoverEvent as? HoverEvent.ShowText ?: return
		val value = hoverEvent.value
		val url = urlRegex.matchEntire(value.unformattedString)?.groupValues?.get(0) ?: return
		if (!isImageUrl(url)) return
		val imageFuture = imageCache[url] ?: return
		if (!imageFuture.isCompleted) return
		val image = imageFuture.getCompleted() ?: return
		it.drawContext.matrices.push()
		val pos = TConfig.position
		pos.applyTransformations(it.drawContext.matrices)
		val scale = min(1F, min((9 * 20F) / image.height, (16 * 20F) / image.width))
		it.drawContext.matrices.scale(scale, scale, 1F)
		it.drawContext.drawTexture(
			image.texture,
			0,
			0,
			1F,
			1F,
			image.width,
			image.height,
			image.width,
			image.height,
		)
		it.drawContext.matrices.pop()
	}

	@Subscribe
	fun onModifyChat(it: ModifyChatEvent) {
		if (!TConfig.enableLinks) return
		it.replaceWith = it.replaceWith.transformEachRecursively { child ->
			val text = child.string
			if ("://" !in text) return@transformEachRecursively child
			val s = Text.empty().setStyle(child.style)
			var index = 0
			while (index < text.length) {
				val nextMatch = urlRegex.find(text, index)
				val url = nextMatch?.groupValues[0]
				val uri = runCatching { url?.let(::URI) }.getOrNull()
				if (nextMatch == null || url == null || uri == null) {
					s.append(Text.literal(text.substring(index, text.length)))
					break
				}
				val range = nextMatch.groups[0]!!.range
				s.append(Text.literal(text.substring(index, range.first)))
				s.append(
					Text.literal(url).setStyle(
						Style.EMPTY.withUnderline(true).withColor(
							Formatting.AQUA
						).withHoverEvent(HoverEvent.ShowText(Text.literal(url)))
							.withClickEvent(ClickEvent.OpenUrl(uri))
					)
				)
				if (isImageUrl(url))
					tryCacheUrl(url)
				index = range.last + 1
			}
			s
		}
	}
}
