/*
 * Firmament is a Hypixel Skyblock mod for modern Minecraft versions
 * Copyright (C) 2023 Linnea Gräf
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.Path
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.Version
import net.fabricmc.loader.api.metadata.ModMetadata
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.dbus.exceptions.DBusException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.coroutines.EmptyCoroutineContext
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.util.Identifier
import moe.nea.firmament.commands.registerFirmamentCommand
import moe.nea.firmament.dbus.FirmamentDbusObject
import moe.nea.firmament.events.ScreenRenderPostEvent
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.features.FeatureManager
import moe.nea.firmament.repo.HypixelStaticData
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.data.IDataHolder

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
        ScreenEvents.AFTER_INIT.register(ScreenEvents.AfterInit { client, screen, scaledWidth, scaledHeight ->
            ScreenEvents.afterRender(screen)
                .register(ScreenEvents.AfterRender { screen, drawContext, mouseX, mouseY, tickDelta ->
                    ScreenRenderPostEvent.publish(ScreenRenderPostEvent(screen, mouseX, mouseY, tickDelta, drawContext))
                })
        })
    }

    fun identifier(path: String) = Identifier(MOD_ID, path)
}
