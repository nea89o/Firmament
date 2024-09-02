package moe.nea.firmament.util

import io.github.moulberry.repo.constants.Islands
import io.github.moulberry.repo.constants.Islands.Warp
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.seconds
import net.minecraft.text.Text
import net.minecraft.util.math.Position
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.commands.thenExecute
import moe.nea.firmament.events.CommandEvent
import moe.nea.firmament.events.ProcessChatEvent
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.data.ProfileSpecificDataHolder

object WarpUtil {
    val warps: List<Islands.Warp> get() = RepoManager.neuRepo.constants.islands.warps

    @Serializable
    data class Data(
        val excludedWarps: MutableSet<String> = mutableSetOf(),
    )

    object DConfig : ProfileSpecificDataHolder<Data>(serializer(), "warp-util", ::Data)

    private var lastAttemptedWarp = ""
    private var lastWarpAttempt = TimeMark.farPast()
    fun findNearestWarp(island: SkyBlockIsland, pos: Position): Islands.Warp? {
        return warps.asSequence().filter { it.mode == island.locrawMode }.minByOrNull {
            if (DConfig.data?.excludedWarps?.contains(it.warp) == true) {
                return@minByOrNull Double.MAX_VALUE
            } else {
                return@minByOrNull squaredDist(pos, it)
            }
        }
    }

    private fun squaredDist(pos: Position, warp: Warp): Double {
        val dx = pos.x - warp.x
        val dy = pos.y - warp.y
        val dz = pos.z - warp.z
        return dx * dx + dy * dy + dz * dz
    }

    fun teleportToNearestWarp(island: SkyBlockIsland, pos: Position) {
        val nearestWarp = findNearestWarp(island, pos)
        if (nearestWarp == null) {
            MC.sendChat(Text.translatable("firmament.warp-util.no-warp-found", island.userFriendlyName))
            return
        }
        if (island == SBData.skyblockLocation
            && sqrt(squaredDist(pos, nearestWarp)) > 1.1 * sqrt(squaredDist((MC.player ?: return).pos, nearestWarp))
        ) {
            MC.sendChat(Text.translatable("firmament.warp-util.already-close", nearestWarp.warp))
            return
        }
        MC.sendChat(Text.translatable("firmament.warp-util.attempting-to-warp", nearestWarp.warp))
        lastWarpAttempt = TimeMark.now()
        lastAttemptedWarp = nearestWarp.warp
        MC.sendServerCommand("warp ${nearestWarp.warp}")
    }

    @Subscribe
    fun clearUnlockedWarpsCommand(event: CommandEvent.SubCommand) {
        event.subcommand("clearwarps") {
            thenExecute {
                DConfig.data?.excludedWarps?.clear()
                DConfig.markDirty()
                source.sendFeedback(Text.translatable("firmament.warp-util.clear-excluded"))
            }
        }
    }

    init {
        ProcessChatEvent.subscribe("WarpUtil:processChat") {
            if (it.unformattedString == "You haven't unlocked this fast travel destination!"
                && lastWarpAttempt.passedTime() < 2.seconds
            ) {
                DConfig.data?.excludedWarps?.add(lastAttemptedWarp)
                DConfig.markDirty()
                MC.sendChat(Text.stringifiedTranslatable("firmament.warp-util.mark-excluded", lastAttemptedWarp))
                lastWarpAttempt = TimeMark.farPast()
            }
            if (it.unformattedString.startsWith("You may now fast travel to")) {
                DConfig.data?.excludedWarps?.clear()
                DConfig.markDirty()
            }
        }
    }
}
