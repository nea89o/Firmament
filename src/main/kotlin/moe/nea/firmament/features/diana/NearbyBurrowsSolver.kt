/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.diana

import kotlin.time.Duration.Companion.seconds
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Position
import moe.nea.firmament.events.ParticleSpawnEvent
import moe.nea.firmament.events.ProcessChatEvent
import moe.nea.firmament.events.WorldReadyEvent
import moe.nea.firmament.events.WorldRenderLastEvent
import moe.nea.firmament.util.TimeMark
import moe.nea.firmament.util.mutableMapWithMaxSize
import moe.nea.firmament.util.render.RenderInWorldContext.Companion.renderInWorld

object NearbyBurrowsSolver {


    private val recentlyDugBurrows: MutableMap<BlockPos, TimeMark> = mutableMapWithMaxSize(20)
    private val recentEnchantParticles: MutableMap<BlockPos, TimeMark> = mutableMapWithMaxSize(500)
    private var lastBlockClick: BlockPos? = null

    enum class BurrowType {
        START, MOB, TREASURE
    }

    val burrows = mutableMapOf<BlockPos, BurrowType>()

    fun onChatEvent(event: ProcessChatEvent) {
        val lastClickedBurrow = lastBlockClick ?: return
        if (event.unformattedString.startsWith("You dug out a Griffin Burrow!") ||
            event.unformattedString.startsWith(" ☠ You were killed by") ||
            event.unformattedString.startsWith("You finished the Griffin burrow chain!")
        ) {
            markAsDug(lastClickedBurrow)
            burrows.remove(lastClickedBurrow)
        }
    }


    fun wasRecentlyDug(blockPos: BlockPos): Boolean {
        val lastDigTime = recentlyDugBurrows[blockPos] ?: TimeMark.farPast()
        return lastDigTime.passedTime() < 10.seconds
    }

    fun markAsDug(blockPos: BlockPos) {
        recentlyDugBurrows[blockPos] = TimeMark.now()
    }

    fun wasRecentlyEnchanted(blockPos: BlockPos): Boolean {
        val lastEnchantTime = recentEnchantParticles[blockPos] ?: TimeMark.farPast()
        return lastEnchantTime.passedTime() < 4.seconds
    }

    fun markAsEnchanted(blockPos: BlockPos) {
        recentEnchantParticles[blockPos] = TimeMark.now()
    }

    fun onParticles(event: ParticleSpawnEvent) {
        if (!DianaWaypoints.TConfig.nearbyWaypoints) return

        val position: BlockPos = event.position.toBlockPos().down()

        if (wasRecentlyDug(position)) return

        val isEven50Spread = (event.offset.x == 0.5f && event.offset.z == 0.5f)

        if (event.particleEffect.type == ParticleTypes.ENCHANT) {
            if (event.count == 5 && event.speed == 0.05F && event.offset.y == 0.4F && isEven50Spread) {
                markAsEnchanted(position)
            }
            return
        }

        if (!wasRecentlyEnchanted(position)) return

        if (event.particleEffect.type == ParticleTypes.ENCHANTED_HIT
            && event.count == 4
            && event.speed == 0.01F
            && event.offset.y == 0.1f
            && isEven50Spread
        ) {
            burrows[position] = BurrowType.START
        }
        if (event.particleEffect.type == ParticleTypes.CRIT
            && event.count == 3
            && event.speed == 0.01F
            && event.offset.y == 0.1F
            && isEven50Spread
        ) {
            burrows[position] = BurrowType.MOB
        }
        if (event.particleEffect.type == ParticleTypes.DRIPPING_LAVA
            && event.count == 2
            && event.speed == 0.01F
            && event.offset.y == 0.1F
            && event.offset.x == 0.35F && event.offset.z == 0.35f
        ) {
            burrows[position] = BurrowType.TREASURE
        }
    }

    fun onRender(event: WorldRenderLastEvent) {
        if (!DianaWaypoints.TConfig.nearbyWaypoints) return
        renderInWorld(event) {
            for ((location, burrow) in burrows) {
                when (burrow) {
                    BurrowType.START -> color(.2f, .8f, .2f, 0.4f)
                    BurrowType.MOB -> color(0.3f, 0.4f, 0.9f, 0.4f)
                    BurrowType.TREASURE -> color(1f, 0.7f, 0.2f, 0.4f)
                }
                block(location)
            }
        }
    }

    fun onSwapWorld(worldReadyEvent: WorldReadyEvent) {
        burrows.clear()
        recentEnchantParticles.clear()
        recentlyDugBurrows.clear()
        lastBlockClick = null
    }

    fun onBlockClick(blockPos: BlockPos) {
        if (!DianaWaypoints.TConfig.nearbyWaypoints) return
        burrows.remove(blockPos)
        lastBlockClick = blockPos
    }
}

fun Position.toBlockPos(): BlockPos {
    return BlockPos(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z))
}
