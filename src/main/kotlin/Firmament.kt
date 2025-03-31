package moe.nea.firmament

import com.google.gson.Gson
import com.mojang.brigadier.CommandDispatcher
import io.ktor.client.HttpClient
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.ResourcePackActivationType
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.Version
import net.fabricmc.loader.api.metadata.ModMetadata
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.plus
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlin.coroutines.EmptyCoroutineContext
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.util.Identifier
import moe.nea.firmament.commands.registerFirmamentCommand
import moe.nea.firmament.events.ClientInitEvent
import moe.nea.firmament.events.ClientStartedEvent
import moe.nea.firmament.events.CommandEvent
import moe.nea.firmament.events.ItemTooltipEvent
import moe.nea.firmament.events.ScreenRenderPostEvent
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.events.registration.registerFirmamentEvents
import moe.nea.firmament.features.FeatureManager
import moe.nea.firmament.repo.HypixelStaticData
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.data.IDataHolder
import moe.nea.firmament.util.tr

object Firmament {
	val modContainer by lazy { FabricLoader.getInstance().getModContainer(MOD_ID).get() }
	const val MOD_ID = "firmament"

	val DEBUG = System.getProperty("firmament.debug") == "true"
	val DATA_DIR: Path = Path.of(".firmament").also { Files.createDirectories(it) }
	val CONFIG_DIR: Path = Path.of("config/firmament").also { Files.createDirectories(it) }
	val logger: Logger = LogManager.getLogger("Firmament")
	private val metadata: ModMetadata by lazy {
		FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().metadata
	}
	val version: Version by lazy { metadata.version }

	@OptIn(ExperimentalSerializationApi::class)
	val json = Json {
		prettyPrint = DEBUG
		isLenient = true
		allowTrailingComma = true
		ignoreUnknownKeys = true
		encodeDefaults = true
	}
	val gson = Gson()
	val tightJson = Json(from = json) {
		prettyPrint = false
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
	val coroutineScope =
		CoroutineScope(EmptyCoroutineContext + CoroutineName("Firmament")) + SupervisorJob(globalJob)

	private fun registerCommands(
		dispatcher: CommandDispatcher<FabricClientCommandSource>,
		@Suppress("UNUSED_PARAMETER")
		ctx: CommandRegistryAccess
	) {
		registerFirmamentCommand(dispatcher)
		CommandEvent.publish(CommandEvent(dispatcher, ctx, MC.networkHandler?.commandDispatcher))
	}

	@JvmStatic
	fun onInitialize() {
	}

	@JvmStatic
	fun onClientInitialize() {
		FeatureManager.subscribeEvents()
		ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { instance ->
			TickEvent.publish(TickEvent(MC.currentTick++))
		})
		IDataHolder.registerEvents()
		RepoManager.initialize()
		SBData.init()
		FeatureManager.autoload()
		HypixelStaticData.spawnDataCollectionLoop()
		ClientCommandRegistrationCallback.EVENT.register(this::registerCommands)
		ClientLifecycleEvents.CLIENT_STARTED.register(ClientLifecycleEvents.ClientStarted {
			ClientStartedEvent.publish(ClientStartedEvent())
		})
		ClientLifecycleEvents.CLIENT_STOPPING.register(ClientLifecycleEvents.ClientStopping {
			logger.info("Shutting down Firmament coroutines")
			globalJob.cancel()
		})
		registerFirmamentEvents()
		ItemTooltipCallback.EVENT.register { stack, context, type, lines ->
			ItemTooltipEvent.publish(ItemTooltipEvent(stack, context, type, lines))
		}
		ScreenEvents.AFTER_INIT.register(ScreenEvents.AfterInit { client, screen, scaledWidth, scaledHeight ->
			ScreenEvents.afterRender(screen)
				.register(ScreenEvents.AfterRender { screen, drawContext, mouseX, mouseY, tickDelta ->
					ScreenRenderPostEvent.publish(ScreenRenderPostEvent(screen, mouseX, mouseY, tickDelta, drawContext))
				})
		})
		ClientInitEvent.publish(ClientInitEvent())
		ResourceManagerHelper.registerBuiltinResourcePack(
			identifier("transparent_overlay"),
			modContainer,
			tr("firmament.resourcepack.transparentoverlay", "Transparent Firmament Overlay"),
			ResourcePackActivationType.NORMAL
		)
	}


	fun identifier(path: String) = Identifier.of(MOD_ID, path)
	inline fun <reified T : Any> tryDecodeJsonFromStream(inputStream: InputStream): Result<T> {
		return runCatching {
			json.decodeFromStream<T>(inputStream)
		}
	}
}
