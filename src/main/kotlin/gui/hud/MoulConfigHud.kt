package moe.nea.firmament.gui.hud

import io.github.notenoughupdates.moulconfig.gui.GuiContext
import io.github.notenoughupdates.moulconfig.gui.component.TextComponent
import io.github.notenoughupdates.moulconfig.platform.MoulConfigScreenComponent
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import net.minecraft.network.chat.Component
import moe.nea.firmament.events.FinalizeResourceManagerEvent
import moe.nea.firmament.events.HudRenderEvent
import moe.nea.firmament.gui.config.HudMeta
import moe.nea.firmament.jarvis.JarvisIntegration
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.MoulConfigUtils

abstract class MoulConfigHud(
	val name: String,
	val hudMeta: HudMeta,
) {
	companion object {
		private val componentWrapper by lazy {
			object : MoulConfigScreenComponent(Component.empty(), GuiContext(TextComponent("§cERROR")), null) {
				init {
					this.minecraft = MC.instance
				}
			}
		}
	}

	private var fragment: GuiContext? = null

	fun forceInit() {
	}

	open fun shouldRender(): Boolean {
		return true
	}

	init {
		require(name.matches("^[a-z_/]+$".toRegex()))
		HudRenderEvent.subscribe("MoulConfigHud:render") {
			if (!shouldRender()) return@subscribe
			val renderContext = componentWrapper.createContext(it.context)
			if (fragment == null)
				loadFragment()
			it.context.pose().pushMatrix()
			hudMeta.applyTransformations(it.context.pose())
			val pos = hudMeta.getEffectivePosition(JarvisIntegration.jarvis)
			val renderContextTranslated =
				renderContext.translated(pos.x(), pos.y(), hudMeta.effectiveWidth, hudMeta.effectiveHeight)
					.scaled(hudMeta.scale)
			fragment!!.root.render(renderContextTranslated)
			it.context.pose().popMatrix()
		}
		FinalizeResourceManagerEvent.subscribe("MoulConfigHud:finalizeResourceManager") {
			MC.resourceManager.registerReloadListener(object : ResourceManagerReloadListener {
				override fun onResourceManagerReload(manager: ResourceManager) {
					fragment = null
				}
			})
		}
	}

	fun loadFragment() {
		fragment = MoulConfigUtils.loadGui(name, this)
	}

}
