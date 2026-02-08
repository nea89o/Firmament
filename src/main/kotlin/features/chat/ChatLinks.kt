package moe.nea.firmament.features.chat

import com.mojang.blaze3d.platform.NativeImage
import java.net.URI
import java.util.Collections
import java.util.concurrent.atomic.AtomicInteger
import org.joml.Vector2i
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.future.await
import kotlin.math.min
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.ActiveTextCollector.ClickableStyleFinder
import net.minecraft.client.gui.components.ChatComponent
import net.minecraft.client.gui.screens.ChatScreen
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.resources.Identifier
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.ModifyChatEvent
import moe.nea.firmament.events.ScreenRenderPostEvent
import moe.nea.firmament.jarvis.JarvisIntegration
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.data.Config
import moe.nea.firmament.util.data.ManagedConfig
import moe.nea.firmament.util.net.HttpUtil
import moe.nea.firmament.util.render.drawTexture
import moe.nea.firmament.util.transformEachRecursively
import moe.nea.firmament.util.unformattedString


object ChatLinks {
	val identifier: String
		get() = "chat-links"

	@Config
	object TConfig : ManagedConfig(identifier, Category.CHAT) {
		val enableLinks by toggle("links-enabled") { true }
		val imageEnabled by toggle("image-enabled") { true }
		val allowAllHosts by toggle("allow-all-hosts") { false }
		val allowedHosts by string("allowed-hosts") { "cdn.discordapp.com,media.discordapp.com,media.discordapp.net,i.imgur.com" }
		val actualAllowedHosts get() = allowedHosts.split(",").map { it.trim() }
		val position by position("position", 16 * 20, 9 * 20) { Vector2i(0, 0) }
	}

	private fun isHostAllowed(host: String) =
		TConfig.allowAllHosts || TConfig.actualAllowedHosts.any { it.equals(host, ignoreCase = true) }

	private fun isUrlAllowed(url: String) = isHostAllowed(url.removePrefix("https://").substringBefore("/"))

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
				val inputStream = HttpUtil.request(url)
					.forInputStream()
					.await()
				val image = NativeImage.read(inputStream)
				val texId = Firmament.identifier("dynamic_image_preview${nextTexId.getAndIncrement()}")
				MC.textureManager.register(
					texId,
					DynamicTexture({ texId.path }, image)
				)
				Image(texId, image.width, image.height)
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

	fun ChatComponent.findComponentAtMousePos(x: Int, y: Int): Style? {
		val clickableStyleFinder = ClickableStyleFinder(MC.font, x,y)
			.includeInsertions(false)
		captureClickableText(clickableStyleFinder, MC.window.guiScaledHeight, MC.instance.gui.guiTicks, true)
		return clickableStyleFinder.result()
	}

	@Subscribe
	@OptIn(ExperimentalCoroutinesApi::class)
	fun onRender(it: ScreenRenderPostEvent) {
		if (!TConfig.imageEnabled) return
		if (it.screen !is ChatScreen) return
		val hoveredComponent =
			MC.inGameHud.chat.findComponentAtMousePos(it.mouseX, it.mouseY) ?: return
		val hoverEvent = hoveredComponent.hoverEvent as? HoverEvent.ShowText ?: return
		val value = hoverEvent.value
		val url = urlRegex.matchEntire(value.unformattedString)?.groupValues?.get(0) ?: return
		if (!isImageUrl(url)) return
		val imageFuture = imageCache[url] ?: return
		if (!imageFuture.isCompleted) return
		val image = imageFuture.getCompleted() ?: return
		it.drawContext.pose().pushMatrix()
		val pos = TConfig.position
		pos.applyTransformations(JarvisIntegration.jarvis, it.drawContext.pose())
		val scale = min(1F, min((9 * 20F) / image.height, (16 * 20F) / image.width))
		it.drawContext.pose().scale(scale, scale)
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
		it.drawContext.pose().popMatrix()
	}

	@Subscribe
	fun onModifyChat(it: ModifyChatEvent) {
		if (!TConfig.enableLinks) return
		it.replaceWith = it.replaceWith.transformEachRecursively { child ->
			val text = child.string
			if ("://" !in text) return@transformEachRecursively child
			val s = Component.empty().setStyle(child.style)
			var index = 0
			while (index < text.length) {
				val nextMatch = urlRegex.find(text, index)
				val url = nextMatch?.groupValues[0]
				val uri = runCatching { url?.let(::URI) }.getOrNull()
				if (nextMatch == null || url == null || uri == null) {
					s.append(Component.literal(text.substring(index, text.length)))
					break
				}
				val range = nextMatch.groups[0]!!.range
				s.append(Component.literal(text.substring(index, range.first)))
				s.append(
					Component.literal(url).setStyle(
						Style.EMPTY.withUnderlined(true).withColor(
							ChatFormatting.AQUA
						).withHoverEvent(HoverEvent.ShowText(Component.literal(url)))
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
