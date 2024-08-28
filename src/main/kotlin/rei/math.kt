

package moe.nea.firmament.rei

import me.shedaniel.math.Point

operator fun Point.plus(other: Point): Point = Point(
    this.x + other.x,
    this.y + other.y,
)
