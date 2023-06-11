package moe.nea.firmament.util

import net.minecraft.text.TextColor
import net.minecraft.util.DyeColor

fun DyeColor.toShedaniel(): me.shedaniel.math.Color =
    me.shedaniel.math.Color.ofOpaque(this.signColor)

fun DyeColor.toTextColor(): TextColor =
    TextColor.fromRgb(this.signColor)

