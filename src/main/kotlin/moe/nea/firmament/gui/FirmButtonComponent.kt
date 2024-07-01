/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.gui

import io.github.notenoughupdates.moulconfig.common.MyResourceLocation
import io.github.notenoughupdates.moulconfig.deps.libninepatch.NinePatch
import io.github.notenoughupdates.moulconfig.gui.GuiComponent
import io.github.notenoughupdates.moulconfig.gui.GuiImmediateContext
import io.github.notenoughupdates.moulconfig.gui.MouseEvent
import io.github.notenoughupdates.moulconfig.gui.component.PanelComponent
import io.github.notenoughupdates.moulconfig.observer.GetSetter


open class FirmButtonComponent(
    child: GuiComponent,
    val isEnabled: GetSetter<Boolean> = GetSetter.constant(true),
    val action: Runnable,
) : PanelComponent(child) {

    /* TODO: make use of vanillas built in nine slicer */
    val hoveredBg =
        NinePatch.builder(MyResourceLocation("minecraft", "textures/gui/sprites/widget/button_highlighted.png"))
            .cornerSize(5)
            .cornerUv(5 / 200F, 5 / 20F)
            .mode(NinePatch.Mode.STRETCHING)
            .build()
    val unhoveredBg = NinePatch.builder(MyResourceLocation("minecraft", "textures/gui/sprites/widget/button.png"))
        .cornerSize(5)
        .cornerUv(5 / 200F, 5 / 20F)
        .mode(NinePatch.Mode.STRETCHING)
        .build()
    val disabledBg =
        NinePatch.builder(MyResourceLocation("minecraft", "textures/gui/sprites/widget/button_disabled.png"))
            .cornerSize(5)
            .cornerUv(5 / 200F, 5 / 20F)
            .mode(NinePatch.Mode.STRETCHING)
            .build()
    val activeBg = NinePatch.builder(MyResourceLocation("firmament", "textures/gui/sprites/widget/button_active.png"))
        .cornerSize(5)
        .cornerUv(5 / 200F, 5 / 20F)
        .mode(NinePatch.Mode.STRETCHING)
        .build()

    var isClicking = false
    override fun mouseEvent(mouseEvent: MouseEvent, context: GuiImmediateContext): Boolean {
        if (!isEnabled.get()) return false
        if (isClicking) {
            if (mouseEvent is MouseEvent.Click && !mouseEvent.mouseState && mouseEvent.mouseButton == 0) {
                isClicking = false
                if (context.isHovered) {
                    action.run()
                }
                return true
            }
        }
        if (!context.isHovered) return false
        if (mouseEvent !is MouseEvent.Click) return false
        if (mouseEvent.mouseState && mouseEvent.mouseButton == 0) {
            isClicking = true
            return true
        }
        return false
    }

    open fun getBackground(context: GuiImmediateContext): NinePatch<MyResourceLocation> =
        if (!isEnabled.get()) disabledBg
        else if (context.isHovered || isClicking) hoveredBg
        else unhoveredBg

    override fun render(context: GuiImmediateContext) {
        context.renderContext.pushMatrix()
        context.renderContext.drawNinePatch(
            getBackground(context),
            0f, 0f, context.width, context.height
        )
        context.renderContext.translate(insets.toFloat(), insets.toFloat(), 0f)
        element.render(getChildContext(context))
        context.renderContext.popMatrix()
    }
}
