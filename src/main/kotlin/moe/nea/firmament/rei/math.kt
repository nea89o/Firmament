/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.rei

import me.shedaniel.math.Point

operator fun Point.plus(other: Point): Point = Point(
    this.x + other.x,
    this.y + other.y,
)
