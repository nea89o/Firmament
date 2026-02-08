package moe.nea.firmament.util

import io.github.moulberry.repo.data.Coordinate
import io.github.notenoughupdates.moulconfig.platform.MoulConfigScreenComponent
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.jvm.optionals.getOrNull
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.player.LocalPlayer
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.NbtOps
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket
import net.minecraft.data.registries.VanillaRegistries
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.core.registries.Registries
import net.minecraft.resources.RegistryOps
import net.minecraft.core.HolderLookup
import net.minecraft.server.packs.resources.ReloadableResourceManager
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.core.BlockPos
import net.minecraft.util.Util
import net.minecraft.world.level.Level
import moe.nea.firmament.Firmament
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.events.WorldReadyEvent
import moe.nea.firmament.util.mc.TolerantRegistriesOps

object MC {

	private val messageQueue = ConcurrentLinkedQueue<Component>()

	init {
		TickEvent.subscribe("MC:push") {
			if (inGameHud.chat != null && world != null)
				while (true) {
					inGameHud.chat.addMessage(messageQueue.poll() ?: break)
				}
			while (true) {
				(nextTickTodos.poll() ?: break).invoke()
			}
		}
		WorldReadyEvent.subscribe("MC:ready") {
			this.lastWorld
		}
	}

	fun sendChat(text: Component) {
		if (TestUtil.isInTest) {
			Firmament.logger.info("CHAT: ${text.string}")
			return
		}
		if (instance.isSameThread && inGameHud.chat != null && world != null)
			inGameHud.chat.addMessage(text)
		else
			messageQueue.add(text)
	}

	@Deprecated("Use checked method instead", replaceWith = ReplaceWith("sendCommand(command)"))
	fun sendServerCommand(command: String) {
		val nh = player?.connection ?: return
		nh.send(
			ServerboundChatCommandPacket(
				command,
			)
		)
	}

	fun sendServerChat(text: String) {
		player?.connection?.sendChat(text)
	}

	fun sendCommand(command: String) {
		// TODO: add a queue to this and sendServerChat
		ErrorUtil.softCheck("Server commands have an implied /", !command.startsWith("/"))
		player?.connection?.sendCommand(command)
	}

	fun onMainThread(block: () -> Unit) {
		if (instance.isSameThread)
			block()
		else
			instance.schedule(block)
	}

	private val nextTickTodos = ConcurrentLinkedQueue<() -> Unit>()
	fun nextTick(function: () -> Unit) {
		nextTickTodos.add(function)
	}


	inline val resourceManager get() = (instance.resourceManager as ReloadableResourceManager)
	inline val itemRenderer: ItemRenderer get() = instance.itemRenderer
	inline val worldRenderer: LevelRenderer get() = instance.levelRenderer
	inline val gameRenderer: GameRenderer get() = instance.gameRenderer
	inline val networkHandler get() = player?.connection
	inline val instance get() = Minecraft.getInstance()
	inline val keyboard get() = instance.keyboardHandler
	inline val interactionManager get() = instance.gameMode
	inline val textureManager get() = instance.textureManager
	inline val options get() = instance.options
	inline val inGameHud: Gui get() = instance.gui
	inline val font get() = instance.font
	inline val soundManager get() = instance.soundManager
	inline val player: LocalPlayer? get() = TestUtil.unlessTesting { instance.player }
	inline val camera: Entity? get() = instance.cameraEntity
	inline val stackInHand: ItemStack get() = player?.mainHandItem ?: ItemStack.EMPTY
	inline val world: ClientLevel? get() = TestUtil.unlessTesting { instance.level }
	inline val playerName: String get() = player?.name?.unformattedString ?: MC.instance.user.name
	inline var screen: Screen?
		get() = TestUtil.unlessTesting { instance.screen }
		set(value) = instance.setScreen(value)
	val screenName get() = screen?.title?.unformattedString?.trim()
	inline val handledScreen: AbstractContainerScreen<*>? get() = instance.screen as? AbstractContainerScreen<*>
	inline val window get() = instance.window
	inline val currentRegistries: HolderLookup.Provider? get() = world?.registryAccess()
	val defaultRegistries: HolderLookup.Provider by lazy { VanillaRegistries.createLookup() }
	val defaultRegistryNbtOps by lazy { RegistryOps.create(NbtOps.INSTANCE, defaultRegistries) }
	inline val currentOrDefaultRegistries get() = currentRegistries ?: defaultRegistries
	val currentOrDefaultRegistryNbtOps get() = TolerantRegistriesOps(NbtOps.INSTANCE, currentOrDefaultRegistries)
	val defaultItems: HolderLookup.RegistryLookup<Item> by lazy { defaultRegistries.lookupOrThrow(Registries.ITEM) }
	var currentTick = 0
	var lastWorld: Level? = null
		get() {
			field = world ?: field
			return field
		}
		private set

	val currentMoulConfigContext
		get() = (screen as? MoulConfigScreenComponent)?.guiContext

	fun openUrl(uri: String) {
		Util.getPlatform().openUri(uri)
	}

	fun <T : Any> unsafeGetRegistryEntry(registry: ResourceKey<out Registry<T>>, identifier: Identifier) =
		unsafeGetRegistryEntry(ResourceKey.create<T>(registry, identifier))


	fun <T : Any> unsafeGetRegistryEntry(registryKey: ResourceKey<T>): T? {
		return currentOrDefaultRegistries
			.lookupOrThrow(registryKey.registryKey())
			.get(registryKey)
			.getOrNull()
			?.value()
	}
}


val Coordinate.blockPos: BlockPos
	get() = BlockPos(x, y, z)
