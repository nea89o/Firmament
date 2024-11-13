package moe.nea.firmament.util

import io.github.moulberry.repo.data.Coordinate
import java.util.concurrent.ConcurrentLinkedQueue
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.hud.InGameHud
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.render.item.ItemRenderer
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.item.Item
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket
import net.minecraft.registry.BuiltinRegistries
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.resource.ReloadableResourceManagerImpl
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.events.WorldReadyEvent

object MC {

	private val messageQueue = ConcurrentLinkedQueue<Text>()

	init {
		TickEvent.subscribe("MC:push") {
			if (inGameHud.chatHud != null && world != null)
				while (true) {
					inGameHud.chatHud.addMessage(messageQueue.poll() ?: break)
				}
			while (true) {
				(nextTickTodos.poll() ?: break).invoke()
			}
		}
		WorldReadyEvent.subscribe("MC:ready") {
			this.lastWorld
		}
	}

	fun sendChat(text: Text) {
		if (instance.isOnThread && inGameHud.chatHud != null && world != null)
			inGameHud.chatHud.addMessage(text)
		else
			messageQueue.add(text)
	}

	fun sendServerCommand(command: String) {
		val nh = player?.networkHandler ?: return
		nh.sendPacket(
			CommandExecutionC2SPacket(
				command,
			)
		)
	}

	fun sendServerChat(text: String) {
		player?.networkHandler?.sendChatMessage(text)
	}

	fun sendCommand(command: String) {
		player?.networkHandler?.sendCommand(command)
	}

	fun onMainThread(block: () -> Unit) {
		if (instance.isOnThread)
			block()
		else
			instance.send(block)
	}

	private val nextTickTodos = ConcurrentLinkedQueue<() -> Unit>()
	fun nextTick(function: () -> Unit) {
		nextTickTodos.add(function)
	}


	inline val resourceManager get() = (instance.resourceManager as ReloadableResourceManagerImpl)
	inline val itemRenderer: ItemRenderer get() = instance.itemRenderer
	inline val worldRenderer: WorldRenderer get() = instance.worldRenderer
	inline val networkHandler get() = player?.networkHandler
	inline val instance get() = MinecraftClient.getInstance()
	inline val keyboard get() = instance.keyboard
	inline val interactionManager get() = instance.interactionManager
	inline val textureManager get() = instance.textureManager
	inline val options get() = instance.options
	inline val inGameHud: InGameHud get() = instance.inGameHud
	inline val font get() = instance.textRenderer
	inline val soundManager get() = instance.soundManager
	inline val player: ClientPlayerEntity? get() = instance.player
	inline val camera: Entity? get() = instance.cameraEntity
	inline val guiAtlasManager get() = instance.guiAtlasManager
	inline val world: ClientWorld? get() = instance.world
	inline var screen: Screen?
		get() = instance.currentScreen
		set(value) = instance.setScreen(value)
	val screenName get() = screen?.title?.unformattedString?.trim()
	inline val handledScreen: HandledScreen<*>? get() = instance.currentScreen as? HandledScreen<*>
	inline val window get() = instance.window
	inline val currentRegistries: RegistryWrapper.WrapperLookup? get() = world?.registryManager
	val defaultRegistries: RegistryWrapper.WrapperLookup by lazy { BuiltinRegistries.createWrapperLookup() }
	inline val currentOrDefaultRegistries get() = currentRegistries ?: defaultRegistries
	val defaultItems: RegistryWrapper.Impl<Item> = defaultRegistries.getOrThrow(RegistryKeys.ITEM)
	var lastWorld: World? = null
		get() {
			field = world ?: field
			return field
		}
		private set
}


val Coordinate.blockPos: BlockPos
	get() = BlockPos(x, y, z)
