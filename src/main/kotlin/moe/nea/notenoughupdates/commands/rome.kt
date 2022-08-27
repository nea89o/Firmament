package moe.nea.notenoughupdates.commands

import com.mojang.brigadier.CommandDispatcher
import io.github.cottonmc.cotton.gui.client.CottonClientScreen
import moe.nea.notenoughupdates.gui.repoGui
import moe.nea.notenoughupdates.repo.RepoManager
import moe.nea.notenoughupdates.util.ScreenUtil.setScreenLater
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.text.Text


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
}


fun registerNeuCommand(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
    val neu = dispatcher.register(neuCommand())
    dispatcher.register(literal("alsoneu") {
        redirect(neu)
    })
}




