/*
 * Firmament is a Hypixel Skyblock mod for modern Minecraft versions
 * Copyright (C) 2023 Linnea Gräf
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package moe.nea.firmament.features.fishing

import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.seconds
import net.minecraft.entity.projectile.FishingBobberEntity
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.math.Vec3d
import moe.nea.firmament.events.ParticleSpawnEvent
import moe.nea.firmament.events.WorldReadyEvent
import moe.nea.firmament.events.WorldRenderLastEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.TimeMark
import moe.nea.firmament.util.render.RenderInWorldContext.Companion.renderInWorld

object FishingWarning : FirmamentFeature {
    override val identifier: String
        get() = "fishing-warning"

    object TConfig : ManagedConfig("fishing-warning") {
        // Display a warning when you are about to hook a fish
        val displayWarning by toggle("display-warning") { false }
        val highlightWakeChain by toggle("highlight-wake-chain") { false }
    }

    override val config: ManagedConfig get() = TConfig


    data class WakeChain(
        val delta: Vec3d,
        val momentum: Vec3d,
        val lastContinued: TimeMark,
    )


    val chains = mutableListOf<WakeChain>()

    private fun areAnglesClose(a: Double, b: Double, tolerance: Double): Boolean {
        var dist = (a - b).absoluteValue
        if (180 < dist) dist = 360 - dist;
        return dist <= tolerance
    }

    private fun calculateAngleFromOffsets(xOffset: Double, zOffset: Double): Double {
        // See also: Vanilla 1.8.9 Fishing particle code.
        var angleX = Math.toDegrees(acos(xOffset / 0.04))
        var angleZ = Math.toDegrees(asin(zOffset / 0.04))
        if (xOffset < 0) {
            // Old: angleZ = 180 - angleZ;
            angleZ = 180 - angleZ
        }
        if (zOffset < 0) {
            angleX = 360 - angleX
        }
        angleX %= 360.0
        angleZ %= 360.0
        if (angleX < 0) angleX += 360.0
        if (angleZ < 0) angleZ += 360.0
        var dist = angleX - angleZ
        if (dist < -180) dist += 360.0
        if (dist > 180) dist -= 360.0
        return angleZ + dist / 2
    }

    private fun toDegrees(d: Double) = d * 180 / Math.PI
    private fun toRadians(d: Double) = d / 180 * Math.PI

    fun isHookPossible(hook: FishingBobberEntity, particlePos: Vec3d, angle1: Double, angle2: Double): Boolean {
        val dx = particlePos.x - hook.pos.x
        val dz = particlePos.z - hook.pos.z
        val dist = sqrt(dx * dx + dz * dz)

        if (dist < 0.2) return true
        val tolerance = toDegrees(atan2(0.03125, dist)) * 1.5
        var angleToHook = toDegrees(atan2(dx, dz)) % 360
        if (angleToHook < 0) angleToHook += 360
        return areAnglesClose(angle1, angleToHook, tolerance) || areAnglesClose(angle2, angleToHook, tolerance)
    }

    val recentParticles = mutableListOf<Pair<Vec3d, TimeMark>>()

    data class Candidate(
        val angle1: Double,
        val angle2: Double,
        val hookOrigin: Vec3d,
        val position: Vec3d,
        val timeMark: TimeMark = TimeMark.now()
    )

    val recentCandidates = mutableListOf<Candidate>()

    private fun onParticleSpawn(event: ParticleSpawnEvent) {
        if (event.particleEffect.type != ParticleTypes.FISHING) return
        if (!(abs(event.offset.y - 0.01f) < 0.001f)) return
        val hook = MC.player?.fishHook ?: return
        val actualOffset = event.offset
        val candidate1 = calculateAngleFromOffsets(actualOffset.x, -actualOffset.z)
        val candidate2 = calculateAngleFromOffsets(-actualOffset.x, actualOffset.z)
        recentCandidates.add(Candidate(candidate1, candidate2, hook.pos, event.position))

        if (isHookPossible(hook, event.position, candidate1, candidate2)) {
            recentParticles.add(Pair(event.position, TimeMark.now()))
        }
    }

    override fun onLoad() {
        ParticleSpawnEvent.subscribe(::onParticleSpawn)
        WorldReadyEvent.subscribe {
            recentParticles.clear()
        }
        WorldRenderLastEvent.subscribe {
            recentParticles.removeIf { it.second.passedTime() > 5.seconds }
            recentCandidates.removeIf { it.timeMark.passedTime() > 5.seconds }
            renderInWorld(it) {
                color(0f, 0f, 1f, 1f)
                recentParticles.forEach {
                    tinyBlock(it.first, 0.1F)
                }

                recentCandidates.forEach {
                    println(it)
                    color(1f, 1f, 0f, 1f)
                    line(it.hookOrigin, it.position)
                    color(1f, 0f, 0f, 1f)
                    line(it.position, Vec3d.fromPolar(0F, it.angle1.toFloat()).add(it.position))
                    color(0f, 1f, 0f, 1f)
                    line(it.position, Vec3d.fromPolar(0F, it.angle2.toFloat()).add(it.position))
                }
            }
        }
    }
}
