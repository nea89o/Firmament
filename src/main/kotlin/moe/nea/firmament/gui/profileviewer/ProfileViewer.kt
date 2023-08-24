/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.gui.profileviewer

import io.github.cottonmc.cotton.gui.client.CottonClientScreen
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription
import io.github.cottonmc.cotton.gui.widget.WTabPanel
import java.util.*
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import kotlinx.coroutines.launch
import net.minecraft.text.Text
import moe.nea.firmament.Firmament
import moe.nea.firmament.apis.Member
import moe.nea.firmament.apis.PlayerData
import moe.nea.firmament.apis.Profile
import moe.nea.firmament.apis.Routes
import moe.nea.firmament.util.ScreenUtil

class ProfileViewer(
    val primaryPlayer: UUID,
    val playerNames: Map<UUID, String>,
    val accountData: Map<UUID, PlayerData>,
    val profile: Profile,
) : LightweightGuiDescription() {

    val member: Member = profile.members[primaryPlayer] ?: error("Primary player not in profile")
    val primaryName: String = playerNames[primaryPlayer] ?: error("Primary player has no name")
    val account: PlayerData = accountData[primaryPlayer] ?: error("Primary player has no data")

    init {
        val panel = WTabPanel().also { rootPanel = it }
        panel.backgroundPainter
        listOf<ProfilePage>(SkillPage, PetsPage)
            .forEach { page ->
                panel.add(page.getElements(this)) {
                    it.icon(page.icon)
                    it.tooltip(page.text)
                }
            }
    }

    companion object {
        fun onCommand(source: FabricClientCommandSource, name: String) {
            source.sendFeedback(Text.translatable("firmament.pv.lookingup", name))
            Firmament.coroutineScope.launch {
                val uuid = Routes.getUUIDForPlayerName(name)
                if (uuid == null) {
                    source.sendError(Text.translatable("firmament.pv.noplayer", name))
                    return@launch
                }
                val name = Routes.getPlayerNameForUUID(uuid) ?: name
                val names = mapOf(uuid to (name))
                val data = Routes.getAccountData(uuid)
                if (data == null) {
                    source.sendError(Text.translatable("firmament.pv.noprofile", name))
                    return@launch
                }
                val accountData = mapOf(data.uuid to data)
                val profiles = Routes.getProfiles(uuid)
                val profile = profiles?.profiles?.find { it.selected }
                if (profile == null) {
                    source.sendFeedback(Text.translatable("firmament.pv.noprofile", name))
                    return@launch
                }
                ScreenUtil.setScreenLater(CottonClientScreen(ProfileViewer(uuid, names, accountData, profile)))
            }
        }
    }
}


