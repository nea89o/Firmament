package moe.nea.notenoughupdates

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import moe.nea.notenoughupdates.repo.RepoManager
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.commands.CommandBuildContext
import net.minecraft.network.chat.Component
import org.apache.logging.log4j.LogManager
import java.nio.file.Files
import java.nio.file.Path
import kotlin.coroutines.EmptyCoroutineContext

object NotEnoughUpdates : ModInitializer, ClientModInitializer {
    const val MOD_ID = "notenoughupdates"

    val DATA_DIR = Path.of(".notenoughupdates").also { Files.createDirectories(it) }
    val DEBUG = System.getenv("notenoughupdates.debug") == "true"
    val logger = LogManager.getLogger("NotEnoughUpdates")
    val metadata by lazy { FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().metadata }
    val version by lazy { metadata.version }

    val json = Json {
        prettyPrint = DEBUG
        ignoreUnknownKeys = true
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
        dispatcher.register(ClientCommandManager.literal("neureload").executes {
            it.source.sendFeedback(Component.literal("Reloading repository from disk. This may lag a bit."))
            RepoManager.neuRepo.reload()
            Command.SINGLE_SUCCESS
        })
    }

    override fun onInitialize() {
        RepoManager.launchAsyncUpdate()
        ClientCommandRegistrationCallback.EVENT.register(this::registerCommands)
    }

    override fun onInitializeClient() {
    }
}
