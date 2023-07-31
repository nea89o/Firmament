/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.world

import io.github.moulberry.repo.data.Coordinate
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderLayer.ALWAYS_DEPTH_TEST
import net.minecraft.client.render.RenderLayer.MultiPhaseParameters
import net.minecraft.client.render.RenderPhase
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.text.Text
import net.minecraft.util.math.Vec3d
import moe.nea.firmament.events.ServerChatLineReceivedEvent
import moe.nea.firmament.events.SkyblockServerUpdateEvent
import moe.nea.firmament.events.WorldRenderLastEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.blockPos
import moe.nea.firmament.util.data.ProfileSpecificDataHolder
import moe.nea.firmament.util.render.RenderInWorldContext
import moe.nea.firmament.util.render.RenderInWorldContext.Companion.renderInWorld
import moe.nea.firmament.util.unformattedString


object FairySouls : FirmamentFeature {


    @Serializable
    data class Data(
        val foundSouls: MutableMap<String, MutableSet<Int>> = mutableMapOf()
    )

    override val config: ManagedConfig
        get() = TConfig

    object DConfig : ProfileSpecificDataHolder<Data>(serializer(), "found-fairysouls", ::Data)


    object TConfig : ManagedConfig("fairy-souls") {
        val displaySouls by toggle("show") { false }
        val resetSouls by button("reset") {
            DConfig.data?.foundSouls?.clear() != null
            updateMissingSouls()
        }
    }


    override val identifier: String get() = "fairy-souls"

    val playerReach = 5
    val playerReachSquared = playerReach * playerReach

    var currentLocationName: String? = null
    var currentLocationSouls: List<Coordinate> = emptyList()
    var currentMissingSouls: List<Coordinate> = emptyList()

    fun updateMissingSouls() {
        currentMissingSouls = emptyList()
        val c = DConfig.data ?: return
        val fi = c.foundSouls[currentLocationName] ?: setOf()
        val cms = currentLocationSouls.toMutableList()
        fi.asSequence().sortedDescending().filter { it in cms.indices }.forEach { cms.removeAt(it) }
        currentMissingSouls = cms
    }

    fun updateWorldSouls() {
        currentLocationSouls = emptyList()
        val loc = currentLocationName ?: return
        currentLocationSouls = RepoManager.neuRepo.constants.fairySouls.soulLocations[loc] ?: return
    }

    fun findNearestClickableSoul(): Coordinate? {
        val player = MC.player ?: return null
        val pos = player.pos
        val location = SBData.skyblockLocation ?: return null
        val soulLocations: List<Coordinate> =
            RepoManager.neuRepo.constants.fairySouls.soulLocations[location] ?: return null
        return soulLocations
            .map { it to it.blockPos.getSquaredDistance(pos) }
            .filter { it.second < playerReachSquared }
            .minByOrNull { it.second }
            ?.first
    }

    private fun markNearestSoul() {
        val nearestSoul = findNearestClickableSoul() ?: return
        val c = DConfig.data ?: return
        val loc = currentLocationName ?: return
        val idx = currentLocationSouls.indexOf(nearestSoul)
        c.foundSouls.computeIfAbsent(loc) { mutableSetOf() }.add(idx)
        DConfig.markDirty()
        updateMissingSouls()
    }

    val NODEPTH: RenderLayer = RenderLayer.of(
        "firmamentnodepth",
        VertexFormats.POSITION_COLOR_TEXTURE,
        VertexFormat.DrawMode.QUADS,
        256,
        true,
        true,
        MultiPhaseParameters.builder()
            .program(RenderPhase.COLOR_PROGRAM)
            .writeMaskState(RenderPhase.COLOR_MASK)
            .depthTest(ALWAYS_DEPTH_TEST)
            .cull(RenderPhase.DISABLE_CULLING)
            .layering(RenderLayer.VIEW_OFFSET_Z_LAYERING)
            .target(RenderPhase.MAIN_TARGET)
            .build(true)
    )

    override fun onLoad() {
        SkyblockServerUpdateEvent.subscribe {
            currentLocationName = it.newLocraw?.skyblockLocation
            updateWorldSouls()
            updateMissingSouls()
        }
        ServerChatLineReceivedEvent.subscribe {
            when (it.text.unformattedString) {
                "You have already found that Fairy Soul!" -> {
                    markNearestSoul()
                }

                "SOUL! You found a Fairy Soul!" -> {
                    markNearestSoul()
                }
            }
        }
        WorldRenderLastEvent.subscribe {
            if (!TConfig.displaySouls) return@subscribe
            renderInWorld(it) {
                text(Vec3d(0.0, 0.0, 0.0), Text.literal("Test String") , Text.literal("Short"), Text.literal("just lik"), verticalAlign = RenderInWorldContext.VerticalAlign.BOTTOM)
                color(1F, 1F, 0F, 0.8F)
                currentMissingSouls.forEach {
                    block(it.blockPos)
                }
                color(1f, 0f, 1f, 1f)
                currentLocationSouls.forEach {
                    wireframeCube(it.blockPos)
                }
            }
        }
    }
}
