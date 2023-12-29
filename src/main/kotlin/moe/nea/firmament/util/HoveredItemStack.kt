/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util

import me.shedaniel.math.impl.PointHelper
import me.shedaniel.rei.api.client.REIRuntime
import me.shedaniel.rei.api.client.gui.widgets.Slot
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.ParentElement
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.ItemStack
import moe.nea.firmament.mixins.accessor.AccessorHandledScreen


val HandledScreen<*>.focusedItemStack: ItemStack?
    get() {
        this as AccessorHandledScreen
        val vanillaSlot = this.focusedSlot_Firmament?.stack
        if (vanillaSlot != null) return vanillaSlot
        val focusedSlot = ScreenRegistry.getInstance().getFocusedStack(this, PointHelper.ofMouse())
        if (focusedSlot != null) return focusedSlot.cheatsAs().value
        var baseElement: Element? = REIRuntime.getInstance().overlay.orElse(null)
        val mx = PointHelper.getMouseFloatingX()
        val my = PointHelper.getMouseFloatingY()
        while (true) {
            if (baseElement is Slot) return baseElement.currentEntry.cheatsAs().value
            if (baseElement !is ParentElement) return null
            baseElement = baseElement.hoveredElement(mx, my).orElse(null)
        }
    }
