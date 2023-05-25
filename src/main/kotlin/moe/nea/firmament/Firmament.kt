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
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import java.awt.Taskbar.Feature
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
import moe.nea.firmament.commands.registerFirmamentCommand
import moe.nea.firmament.dbus.FirmamentDbusObject
import moe.nea.firmament.features.FeatureManager
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.data.IDataHolder

object Firmament : ModInitializer, ClientModInitializer {
    const val MOD_ID = "firmament"

    val DEBUG = System.getProperty("firmament.debug") == "true"
    val DATA_DIR: Path = Path.of(".firmament").also { Files.createDirectories(it) }
    val CONFIG_DIR: Path = Path.of("config/firmament").also { Files.createDirectories(it) }
    val logger = LogManager.getLogger("Firmament")
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
                agent = "Firmament/$version"
            }
        }
    }

    val globalJob = Job()
    val dbusConnection = DBusConnectionBuilder.forSessionBus()
        .build()
    val coroutineScope =
        CoroutineScope(EmptyCoroutineContext + CoroutineName("Firmament")) + SupervisorJob(globalJob)

    private fun registerCommands(
        dispatcher: CommandDispatcher<FabricClientCommandSource>,
        @Suppress("UNUSED_PARAMETER")
        ctx: CommandRegistryAccess
    ) {
        registerFirmamentCommand(dispatcher)
    }

    override fun onInitialize() {
    }

    override fun onInitializeClient() {
        dbusConnection.requestBusName("moe.nea.firmament")
        dbusConnection.exportObject(FirmamentDbusObject)
        IDataHolder.registerEvents()
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
}
