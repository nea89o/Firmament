package moe.nea.notenoughupdates

import com.mojang.brigadier.CommandDispatcher
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import java.nio.file.Files
import java.nio.file.Path
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.Version
import net.fabricmc.loader.api.metadata.ModMetadata
import org.apache.logging.log4j.LogManager
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlin.coroutines.EmptyCoroutineContext
import net.minecraft.command.CommandRegistryAccess
import moe.nea.notenoughupdates.commands.registerNeuCommand
import moe.nea.notenoughupdates.dbus.NEUDbusObject
import moe.nea.notenoughupdates.features.FeatureManager
import moe.nea.notenoughupdates.repo.RepoManager
import moe.nea.notenoughupdates.util.SBData
import moe.nea.notenoughupdates.util.config.IConfigHolder

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
    val dbusConnection = DBusConnectionBuilder.forSessionBus()
        .build()
    val coroutineScope =
        CoroutineScope(EmptyCoroutineContext + CoroutineName("NotEnoughUpdates")) + SupervisorJob(globalJob)

    private fun registerCommands(
        dispatcher: CommandDispatcher<FabricClientCommandSource>,
        @Suppress("UNUSED_PARAMETER")
        _ctx: CommandRegistryAccess
    ) {
        registerNeuCommand(dispatcher)
    }

    override fun onInitialize() {
        dbusConnection.requestBusName("moe.nea.notenoughupdates")
        dbusConnection.exportObject(NEUDbusObject)
        IConfigHolder.registerEvents()
        RepoManager.initialize()
        SBData.init()
        FeatureManager.autoload()
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
