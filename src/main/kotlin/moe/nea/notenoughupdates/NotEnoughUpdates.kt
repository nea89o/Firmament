package moe.nea.notenoughupdates

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import io.github.cottonmc.cotton.gui.client.CottonClientScreen
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import moe.nea.notenoughupdates.gui.repoGui
import moe.nea.notenoughupdates.repo.RepoManager
import moe.nea.notenoughupdates.util.ConfigHolder
import moe.nea.notenoughupdates.util.ScreenUtil.setScreenLater
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.Version
import net.fabricmc.loader.api.metadata.ModMetadata
import net.minecraft.commands.CommandBuildContext
import net.minecraft.network.chat.Component
import org.apache.logging.log4j.LogManager
import java.nio.file.Files
import java.nio.file.Path
import kotlin.coroutines.EmptyCoroutineContext

object NotEnoughUpdates : ModInitializer, ClientModInitializer {
    const val MOD_ID = "notenoughupdates"

    val DEBUG = System.getProperty("notenoughupdates.debug") == "true"
    val DATA_DIR: Path = Path.of(".notenoughupdates").also { Files.createDirectories(it) }
    val CONFIG_DIR: Path = Path.of("config/notenoughupdates").also { Files.createDirectories(it) }
    val logger = LogManager.getLogger("NotEnoughUpdates")
    val metadata: ModMetadata by lazy { FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().metadata }
    val version: Version by lazy { metadata.version }

    val json = Json {
        prettyPrint = DEBUG
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    val httpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(json)
            }
            install(UserAgent) {
                agent = "NotEnoughUpdates1.19/$version"
            }
        }
    }

    val globalJob = Job()
    val coroutineScope =
        CoroutineScope(EmptyCoroutineContext + CoroutineName("NotEnoughUpdates")) + SupervisorJob(globalJob)
    val coroutineScopeIo = coroutineScope + Dispatchers.IO + SupervisorJob(globalJob)

    private fun registerCommands(
        dispatcher: CommandDispatcher<FabricClientCommandSource>,
        @Suppress("UNUSED_PARAMETER")
        _ctx: CommandBuildContext
    ) {
        dispatcher.register(ClientCommandManager.literal("neureload")
            .then(ClientCommandManager.literal("fetch").executes {
                it.source.sendFeedback(Component.literal("Trying to redownload the repository")) // TODO better reporting
                RepoManager.launchAsyncUpdate()
                Command.SINGLE_SUCCESS
            })
            .executes {
                it.source.sendFeedback(Component.literal("Reloading repository from disk. This may lag a bit."))
                RepoManager.reload()
                Command.SINGLE_SUCCESS
            })
        dispatcher.register(
            ClientCommandManager.literal("neu")
                .then(ClientCommandManager.literal("repo").executes {
                    setScreenLater(CottonClientScreen(repoGui()))
                    Command.SINGLE_SUCCESS
                })
        )
    }

    override fun onInitialize() {
        RepoManager.initialize()
        ConfigHolder.registerEvents()
        ClientCommandRegistrationCallback.EVENT.register(this::registerCommands)
        ClientLifecycleEvents.CLIENT_STOPPING.register(ClientLifecycleEvents.ClientStopping {
            runBlocking {
                logger.info("Shutting down NEU coroutines")
                globalJob.cancel()
            }
        })
    }

    override fun onInitializeClient() {
    }
}
