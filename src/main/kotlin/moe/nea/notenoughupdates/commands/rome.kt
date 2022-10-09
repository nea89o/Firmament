package moe.nea.notenoughupdates.commands

import com.mojang.brigadier.CommandDispatcher
import io.github.cottonmc.cotton.gui.client.CottonClientScreen
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.text.Text
import moe.nea.notenoughupdates.gui.repoGui
import moe.nea.notenoughupdates.repo.RepoManager
import moe.nea.notenoughupdates.util.SBData
import moe.nea.notenoughupdates.util.ScreenUtil.setScreenLater


fun neuCommand() = literal("neu") {
    thenLiteral("reload") {
        thenLiteral("fetch") {
            thenExecute {
                source.sendFeedback(Text.translatable("notenoughupdates.repo.reload.network")) // TODO better reporting
                RepoManager.launchAsyncUpdate()
            }
        }
        thenExecute {
            source.sendFeedback(Text.translatable("notenoughupdates.repo.reload.disk"))
            RepoManager.reload()
        }
    }
    thenLiteral("repo") {
        thenExecute {
            setScreenLater(CottonClientScreen(repoGui()))
        }
    }
    thenLiteral("dev") {
        thenLiteral("sbdata") {
            thenExecute {
                source.sendFeedback(Text.translatable("notenoughupdates.sbinfo.profile", SBData.profileCuteName))
                val locrawInfo = SBData.locraw
                if (locrawInfo == null) {
                    source.sendFeedback(Text.translatable("notenoughupdates.sbinfo.nolocraw"))
                } else {
                    source.sendFeedback(Text.translatable("notenoughupdates.sbinfo.server", locrawInfo.server))
                    source.sendFeedback(Text.translatable("notenoughupdates.sbinfo.gametype", locrawInfo.gametype))
                    source.sendFeedback(Text.translatable("notenoughupdates.sbinfo.mode", locrawInfo.mode))
                    source.sendFeedback(Text.translatable("notenoughupdates.sbinfo.map", locrawInfo.map))
                }

            }
        }
    }
}


fun registerNeuCommand(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
    val neu = dispatcher.register(neuCommand())
    dispatcher.register(literal("alsoneu") {
        redirect(neu)
    })
}




