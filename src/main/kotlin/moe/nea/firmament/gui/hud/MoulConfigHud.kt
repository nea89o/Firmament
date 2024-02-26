/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.gui.hud

import io.github.notenoughupdates.moulconfig.gui.GuiContext
import io.github.notenoughupdates.moulconfig.gui.component.TextComponent
import io.github.notenoughupdates.moulconfig.gui.GuiComponentWrapper
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.SynchronousResourceReloader
import moe.nea.firmament.events.HudRenderEvent
import moe.nea.firmament.gui.config.HudMeta
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.MoulConfigUtils

abstract class MoulConfigHud(
    val name: String,
    val hudMeta: HudMeta,
) {
    companion object {
        private val componentWrapper = object : GuiComponentWrapper(GuiContext(TextComponent("§cERROR"))) {
            init {
                this.client = MC.instance
            }
        }
    }

    private var fragment: GuiContext? = null

    open fun shouldRender(): Boolean {
        return true
    }

    init {
        HudRenderEvent.subscribe {
            if (!shouldRender()) return@subscribe
            val renderContext = componentWrapper.createContext(it.context)
            if (fragment == null)
                loadFragment()
            it.context.matrices.push()
            hudMeta.applyTransformations(it.context.matrices)
            val renderContextTranslated =
                renderContext.translated(hudMeta.absoluteX, hudMeta.absoluteY, hudMeta.width, hudMeta.height)
                    .scaled(hudMeta.scale)
            fragment!!.root.render(renderContextTranslated)
            it.context.matrices.pop()
        }
        MC.resourceManager.registerReloader(object : SynchronousResourceReloader {
            override fun reload(manager: ResourceManager?) {
                fragment = null
            }
        })
    }

    fun loadFragment() {
        fragment = MoulConfigUtils.loadGui(name, this)
    }

}
