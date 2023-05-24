package moe.nea.firmament.commands

import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.text.Text
import moe.nea.firmament.features.world.FairySouls
import moe.nea.firmament.gui.config.AllConfigsGui
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.SBData


fun firmamentCommand() = literal("firmament") {
    thenLiteral("config") {
        thenExecute {
            AllConfigsGui.showAllGuis()
        }
    }
    thenLiteral("repo") {
        thenLiteral("reload") {
            thenLiteral("fetch") {
                thenExecute {
                    source.sendFeedback(Text.translatable("firmament.repo.reload.network")) // TODO better reporting
                    RepoManager.launchAsyncUpdate()
                }
            }
            thenExecute {
                source.sendFeedback(Text.translatable("firmament.repo.reload.disk"))
                RepoManager.reload()
            }
        }
    }
    thenLiteral("dev") {
        thenLiteral("config") {
            thenExecute {
                FairySouls.TConfig.showConfigEditor()
            }
        }
        thenLiteral("sbdata") {
            thenExecute {
                source.sendFeedback(Text.translatable("firmament.sbinfo.profile", SBData.profileCuteName))
                val locrawInfo = SBData.locraw
                if (locrawInfo == null) {
                    source.sendFeedback(Text.translatable("firmament.sbinfo.nolocraw"))
                } else {
                    source.sendFeedback(Text.translatable("firmament.sbinfo.server", locrawInfo.server))
                    source.sendFeedback(Text.translatable("firmament.sbinfo.gametype", locrawInfo.gametype))
                    source.sendFeedback(Text.translatable("firmament.sbinfo.mode", locrawInfo.mode))
                    source.sendFeedback(Text.translatable("firmament.sbinfo.map", locrawInfo.map))
                }

            }
        }
    }
}


fun registerFirmamentCommand(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
    val firmament = dispatcher.register(firmamentCommand())
    dispatcher.register(literal("firm") {
        redirect(firmament)
    })
}




