/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.fishing

import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.seconds
import net.minecraft.entity.projectile.FishingBobberEntity
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.math.Vec3d
import moe.nea.firmament.Firmament
import moe.nea.firmament.events.ParticleSpawnEvent
import moe.nea.firmament.events.WorldReadyEvent
import moe.nea.firmament.events.WorldRenderLastEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.features.debug.DebugView
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
        var angleX = Math.toDegrees(Math.acos(xOffset / 0.04))
        var angleZ = Math.toDegrees(Math.asin(zOffset / 0.04))
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
        return Math.toDegrees(Math.atan2(xOffset, zOffset))
        return angleZ + dist / 2 + 180
    }

    val π = Math.PI
    val τ = Math.PI * 2

    private fun toDegrees(d: Double) = Math.toDegrees(d).mod(360.0)
    private fun toRadians(d: Double) = Math.toRadians(d).mod(τ)

    fun isHookPossible(hook: FishingBobberEntity, particlePos: Vec3d, angle1: Double, angle2: Double): Boolean {
        val dx = particlePos.x - hook.trackedPosition.withDelta(0, 0, 0).x
        val dz = particlePos.z - hook.trackedPosition.withDelta(0, 0, 0).z
        val dist = sqrt(dx * dx + dz * dz)

        if (dist < 0.2) return true
        val tolerance = toDegrees(atan2(0.03125, dist)) * 1.5
        val angleToHook = toDegrees(atan2(dz, dx))
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
        val candidate1 = calculateAngleFromOffsets(-actualOffset.x, (-actualOffset.z))
        val candidate2 = calculateAngleFromOffsets(actualOffset.x, actualOffset.z)
        recentCandidates.add(Candidate(candidate1, candidate2, hook.trackedPosition.withDelta(0, 0, 0), event.position))

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

                if (Firmament.DEBUG) {
                    recentCandidates.forEach {
                        color(1f, 1f, 0f, 1f)
                        line(it.hookOrigin, it.position)
                        color(1f, 0f, 0f, 1f)
                        fun P(yaw: Double) = Vec3d(cos(yaw), 0.0, sin(yaw))
                        line(
                            it.position,
                            P(π - toRadians(it.angle1)).multiply(5.0).add(it.position)
                        )
                        color(0f, 1f, 0f, 1f)
                        line(
                            it.position,
                            P(π - toRadians(it.angle2)).multiply(5.0).add(it.position)
                        )
                        val tolerance = (atan2(0.03125, it.position.distanceTo(it.hookOrigin))).absoluteValue * 1.5
                        val diff = it.hookOrigin.subtract(it.position)
                        val rd = atan2(diff.z, diff.x).mod(τ)
                        color(0.8f, 0f, 0.8f, 1f)
                        DebugView.showVariable("tolerance", tolerance)
                        DebugView.showVariable("angle1Rad", toRadians(180 - it.angle1))
                        DebugView.showVariable("angle1Diff", (toRadians(it.angle1) - rd).mod(τ))
                        DebugView.showVariable("angle1Deg", it.angle1.mod(360.0))
                        DebugView.showVariable("angle2Rad", toRadians(180 - it.angle2))
                        DebugView.showVariable("angle2Deg", it.angle2.mod(360.0))
                        DebugView.showVariable("angle2Diff", (toRadians(it.angle2) - rd).mod(τ))
                        DebugView.showVariable("rd", rd)
                        DebugView.showVariable("minT", (rd + tolerance).mod(τ))
                        DebugView.showVariable("maxT", (rd - tolerance).mod(τ))
                        DebugView.showVariable(
                            "passes",
                            if (min(
                                    (rd - toRadians(180 - it.angle2)).mod(τ),
                                    (rd - toRadians(180 - it.angle1)).mod(τ)
                                ) < tolerance
                            ) {
                                "§aPasses"
                            } else {
                                "§cNo Pass"
                            }
                        )

                        line(it.position, P(rd + tolerance).add(it.position))
                        line(it.position, P(rd - tolerance).add(it.position))
                    }
                    color(0.8F, 0.8F, 0.8f, 1f)
                    val fishHook = MC.player?.fishHook
                    if (fishHook != null)
                        tinyBlock(fishHook.trackedPosition.withDelta(0, 0, 0), 0.2f)
                }
            }
        }
    }
}
