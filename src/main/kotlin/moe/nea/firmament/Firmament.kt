/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament

import com.mojang.brigadier.CommandDispatcher
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import moe.nea.firmament.commands.registerFirmamentCommand
import moe.nea.firmament.dbus.FirmamentDbusObject
import moe.nea.firmament.events.ItemTooltipEvent
import moe.nea.firmament.events.ScreenRenderPostEvent
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.events.registration.registerFirmamentChatEvents
import moe.nea.firmament.features.FeatureManager
import moe.nea.firmament.repo.HypixelStaticData
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.data.IDataHolder
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.Version
import net.fabricmc.loader.api.metadata.ModMetadata
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.dbus.exceptions.DBusException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.coroutines.EmptyCoroutineContext

object Firmament {
    const val MOD_ID = "firmament"

    val DEBUG = System.getProperty("firmament.debug") == "true"
    val DATA_DIR: Path = Path.of(".firmament").also { Files.createDirectories(it) }
    val CONFIG_DIR: Path = Path.of("config/firmament").also { Files.createDirectories(it) }
    val logger: Logger = LogManager.getLogger("Firmament")
    private val metadata: ModMetadata by lazy {
        FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().metadata
    }
    val version: Version by lazy { metadata.version }

    val json = Json {
        prettyPrint = DEBUG
        isLenient = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    val httpClient by lazy {
        HttpClient {
            install(ContentNegotiation) {
                json(json)
            }
            install(ContentEncoding) {
                gzip()
                deflate()
            }
            install(UserAgent) {
                agent = "Firmament/$version"
            }
            if (DEBUG)
                install(Logging) {
                    level = LogLevel.INFO
                }
            install(HttpCache)
        }
    }

    val globalJob = Job()
    val dbusConnection = try {
        DBusConnectionBuilder.forSessionBus()
            .build()
    } catch (e: Exception) {
        null
    }
    val coroutineScope =
        CoroutineScope(EmptyCoroutineContext + CoroutineName("Firmament")) + SupervisorJob(globalJob)

    private fun registerCommands(
        dispatcher: CommandDispatcher<FabricClientCommandSource>,
        @Suppress("UNUSED_PARAMETER")
        ctx: CommandRegistryAccess
    ) {
        registerFirmamentCommand(dispatcher)
    }

    @JvmStatic
    fun onInitialize() {
    }

    @JvmStatic
    fun onClientInitialize() {
        try {
            dbusConnection?.exportObject(FirmamentDbusObject)
            dbusConnection?.requestBusName("moe.nea.firmament")
        } catch (e: DBusException) {
            // :(
        }
        var tick = 0
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { instance ->
            TickEvent.publish(TickEvent(tick++))
        })
        IDataHolder.registerEvents()
        RepoManager.initialize()
        SBData.init()
        FeatureManager.autoload()
        HypixelStaticData.spawnDataCollectionLoop()
        ClientCommandRegistrationCallback.EVENT.register(this::registerCommands)
        ClientLifecycleEvents.CLIENT_STOPPING.register(ClientLifecycleEvents.ClientStopping {
            runBlocking {
                logger.info("Shutting down NEU coroutines")
                globalJob.cancel()
            }
        })
        registerFirmamentChatEvents()
        ItemTooltipCallback.EVENT.register { a, b, c ->
            ItemTooltipEvent.publish(ItemTooltipEvent(a, b, c))
        }
        ScreenEvents.AFTER_INIT.register(ScreenEvents.AfterInit { client, screen, scaledWidth, scaledHeight ->
            ScreenEvents.afterRender(screen)
                .register(ScreenEvents.AfterRender { screen, drawContext, mouseX, mouseY, tickDelta ->
                    ScreenRenderPostEvent.publish(ScreenRenderPostEvent(screen, mouseX, mouseY, tickDelta, drawContext))
                })
        })
    }


    fun identifier(path: String) = Identifier(MOD_ID, path)
}
