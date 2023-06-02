package moe.nea.firmament.gui.profileviewer

import io.github.cottonmc.cotton.gui.client.CottonClientScreen
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription
import io.github.cottonmc.cotton.gui.widget.WTabPanel
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.URLProtocol
import io.ktor.http.path
import java.util.UUID
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import kotlinx.coroutines.launch
import net.minecraft.text.Text
import moe.nea.firmament.Firmament
import moe.nea.firmament.apis.AshconNameLookup
import moe.nea.firmament.apis.Member
import moe.nea.firmament.apis.PlayerData
import moe.nea.firmament.apis.PlayerResponse
import moe.nea.firmament.apis.Profile
import moe.nea.firmament.apis.Profiles
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
        listOf<ProfilePage>(SkillPage)
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
                val nameData = Firmament.httpClient.get("https://api.ashcon.app/mojang/v2/user/$name").body<AshconNameLookup>()
                val names = mapOf(nameData.uuid to nameData.username)
                val data = Firmament.httpClient.get {
                    url {
                        protocol = URLProtocol.HTTPS
                        host = "api.hypixel.net"
                        path("player")
                        parameter("key", "e721a103-96e0-400f-af2a-73b2a91007b1")
                        parameter("uuid", nameData.uuid)
                    }
                }.body<PlayerResponse>()
                val accountData = mapOf(data.player.uuid to data.player)
                val playerUuid = data.player.uuid
                val profiles = Firmament.httpClient.get {
                    url {
                        protocol = URLProtocol.HTTPS
                        host = "api.hypixel.net"
                        path("skyblock", "profiles")
                        parameter("key", "e721a103-96e0-400f-af2a-73b2a91007b1")
                        parameter("uuid", playerUuid)
                    }
                }.body<Profiles>()
                val profile = profiles.profiles.find { it.selected }
                if (profile == null) {
                    source.sendFeedback(Text.translatable("firmament.pv.noprofile", name))
                    return@launch
                }
                ScreenUtil.setScreenLater(
                    CottonClientScreen(
                        ProfileViewer(
                            playerUuid, names, accountData, profile
                        )
                    )
                )
            }
        }
    }
}


