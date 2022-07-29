package moe.nea.notenoughupdates

import com.mojang.brigadier.CommandDispatcher
import io.github.moulberry.repo.NEURepository
import moe.nea.notenoughupdates.repo.ItemCache
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.Minecraft
import net.minecraft.commands.CommandBuildContext
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket
import java.nio.file.Path

object NotEnoughUpdates : ModInitializer, ClientModInitializer {
    val DATA_DIR = Path.of(".notenoughupdates")

    const val MOD_ID = "notenoughupdates"

    val neuRepo: NEURepository = NEURepository.of(Path.of("NotEnoughUpdates-REPO")).apply {
        registerReloadListener(ItemCache)
        reload()
        registerReloadListener {
            Minecraft.getInstance().connection?.handleUpdateRecipes(ClientboundUpdateRecipesPacket(mutableListOf()))
        }
    }

    fun registerCommands(
        dispatcher: CommandDispatcher<FabricClientCommandSource>, registryAccess: CommandBuildContext
    ) {
        dispatcher.register(ClientCommandManager.literal("neureload").executes {
            it.source.sendFeedback(Component.literal("Reloading repository from disk. This may lag a bit."))
            neuRepo.reload()
            0
        })

    }

    override fun onInitialize() {
        ClientCommandRegistrationCallback.EVENT.register(this::registerCommands)
    }

    override fun onInitializeClient() {
    }
}
