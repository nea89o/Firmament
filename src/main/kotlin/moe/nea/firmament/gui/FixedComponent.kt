/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.gui

import io.github.notenoughupdates.moulconfig.gui.GuiComponent
import io.github.notenoughupdates.moulconfig.gui.GuiImmediateContext
import io.github.notenoughupdates.moulconfig.gui.KeyboardEvent
import io.github.notenoughupdates.moulconfig.gui.MouseEvent
import io.github.notenoughupdates.moulconfig.observer.GetSetter
import java.util.function.BiFunction

class FixedComponent(
    val fixedWidth: GetSetter<Int>,
    val fixedHeight: GetSetter<Int>,
    val component: GuiComponent,
) : GuiComponent() {
    override fun getWidth(): Int = fixedWidth.get()

    override fun getHeight(): Int = fixedHeight.get()

    override fun <T : Any?> foldChildren(initial: T, visitor: BiFunction<GuiComponent, T, T>): T {
        return visitor.apply(component, initial)
    }

    fun fixContext(context: GuiImmediateContext): GuiImmediateContext =
        context.translated(0, 0, fixedWidth.get(), fixedHeight.get())

    override fun render(context: GuiImmediateContext) {
        component.render(fixContext(context))
    }

    override fun mouseEvent(mouseEvent: MouseEvent, context: GuiImmediateContext): Boolean {
        return component.mouseEvent(mouseEvent, fixContext(context))
    }

    override fun keyboardEvent(event: KeyboardEvent, context: GuiImmediateContext): Boolean {
        return component.keyboardEvent(event, fixContext(context))
    }
}
