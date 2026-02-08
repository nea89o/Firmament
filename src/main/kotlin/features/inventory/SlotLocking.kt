@file:UseSerializers(DashlessUUIDSerializer::class)

package moe.nea.firmament.features.inventory

import java.util.UUID
import org.lwjgl.glfw.GLFW
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.serializer
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import net.minecraft.world.inventory.ChestMenu
import net.minecraft.world.inventory.InventoryMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.inventory.ClickType
import net.minecraft.resources.Identifier
import net.minecraft.util.StringRepresentable
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.ClientInitEvent
import moe.nea.firmament.events.HandledScreenForegroundEvent
import moe.nea.firmament.events.HandledScreenKeyPressedEvent
import moe.nea.firmament.events.HandledScreenKeyReleasedEvent
import moe.nea.firmament.events.IsSlotProtectedEvent
import moe.nea.firmament.events.ScreenChangeEvent
import moe.nea.firmament.events.SlotRenderEvents
import moe.nea.firmament.keybindings.InputModifiers
import moe.nea.firmament.keybindings.SavedKeyBinding
import moe.nea.firmament.mixins.accessor.AccessorHandledScreen
import moe.nea.firmament.util.CommonSoundEffects
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.SkyBlockIsland
import moe.nea.firmament.util.accessors.castAccessor
import moe.nea.firmament.util.data.Config
import moe.nea.firmament.util.data.ManagedConfig
import moe.nea.firmament.util.data.ProfileSpecificDataHolder
import moe.nea.firmament.util.extraAttributes
import moe.nea.firmament.util.json.DashlessUUIDSerializer
import moe.nea.firmament.util.lime
import moe.nea.firmament.util.mc.ScreenUtil.getSlotByIndex
import moe.nea.firmament.util.mc.SlotUtils.swapWithHotBar
import moe.nea.firmament.util.mc.displayNameAccordingToNbt
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.red
import moe.nea.firmament.util.render.drawAlignedBox
import moe.nea.firmament.util.render.drawLine
import moe.nea.firmament.util.skyBlockId
import moe.nea.firmament.util.skyblock.DungeonUtil
import moe.nea.firmament.util.skyblock.SkyBlockItems
import moe.nea.firmament.util.skyblockUUID
import moe.nea.firmament.util.tr
import moe.nea.firmament.util.unformattedString

object SlotLocking {
	val identifier: String
		get() = "slot-locking"

	@Serializable
	data class DimensionData(
		val lockedSlots: MutableSet<Int> = mutableSetOf(),
		val boundSlots: BoundSlots = BoundSlots(),
	)

	@Serializable
	data class Data(
		val lockedUUIDs: MutableSet<UUID> = mutableSetOf(),
		val rift: DimensionData = DimensionData(),
		val overworld: DimensionData = DimensionData(),
	)


	val currentWorldData
		get() = if (SBData.skyblockLocation == SkyBlockIsland.RIFT)
			DConfig.data.rift
		else
			DConfig.data.overworld

	@Serializable
	data class BoundSlot(
		val hotbar: Int,
		val inventory: Int,
	)

	@Serializable(with = BoundSlots.Serializer::class)
	data class BoundSlots(
		val pairs: MutableSet<BoundSlot> = mutableSetOf()
	) {
		fun findMatchingSlots(index: Int): List<BoundSlot> {
			return pairs.filter { it.hotbar == index || it.inventory == index }
		}

		fun removeDuplicateForInventory(index: Int) {
			pairs.removeIf { it.inventory == index }
		}

		fun removeAllInvolving(index: Int): Boolean {
			return pairs.removeIf { it.inventory == index || it.hotbar == index }
		}

		fun insert(hotbar: Int, inventory: Int) {
			if (!TConfig.allowMultiBinding) {
				removeAllInvolving(hotbar)
				removeAllInvolving(inventory)
			}
			pairs.add(BoundSlot(hotbar, inventory))
		}

		object Serializer : KSerializer<BoundSlots> {
			override val descriptor: SerialDescriptor
				get() = serializer<JsonElement>().descriptor

			override fun serialize(
				encoder: Encoder,
				value: BoundSlots
			) {
				serializer<MutableSet<BoundSlot>>()
					.serialize(encoder, value.pairs)
			}

			override fun deserialize(decoder: Decoder): BoundSlots {
				decoder as JsonDecoder
				val json = decoder.decodeJsonElement()
				if (json is JsonObject) {
					return BoundSlots(json.entries.map {
						BoundSlot(it.key.toInt(), (it.value as JsonPrimitive).int)
					}.toMutableSet())
				}
				return BoundSlots(decoder.json.decodeFromJsonElement(serializer<MutableSet<BoundSlot>>(), json))

			}
		}
	}


	@Config
	object TConfig : ManagedConfig(identifier, Category.INVENTORY) {
		val lockSlot by keyBinding("lock") { GLFW.GLFW_KEY_L }
		val lockUUID by keyBindingWithOutDefaultModifiers("lock-uuid") {
			SavedKeyBinding.keyWithMods(GLFW.GLFW_KEY_L, InputModifiers.of(shift = true))
		}
		val slotBind by keyBinding("bind") { GLFW.GLFW_KEY_L }
		val lockBound by toggle("lock-bind") { false }
		val slotBindRequireShift by toggle("require-quick-move") { true }
		val slotRenderLines by choice("bind-render") { SlotRenderLinesMode.ONLY_BOXES }
		val slotBindOnlyInInv by toggle("bind-only-in-inv") { false }
		val allowMultiBinding by toggle("multi-bind") { true } // TODO: filter based on this option
		val protectAllHuntingBoxes by toggle("hunting-box") { false }
		val allowDroppingInDungeons by toggle("drop-in-dungeons") { true }
	}

	enum class SlotRenderLinesMode : StringRepresentable {
		EVERYTHING,
		ONLY_BOXES,
		NOTHING;

		override fun getSerializedName(): String {
			return name
		}
	}

	@Config
	object DConfig : ProfileSpecificDataHolder<Data>(serializer(), "locked-slots", ::Data)

	val lockedUUIDs get() = DConfig.data?.lockedUUIDs

	val lockedSlots
		get() = currentWorldData?.lockedSlots

	fun isSalvageScreen(screen: AbstractContainerScreen<*>?): Boolean {
		if (screen == null) return false
		return screen.title.unformattedString.contains("Salvage Item")
	}

	fun isTradeScreen(screen: AbstractContainerScreen<*>?): Boolean {
		if (screen == null) return false
		val handler = screen.menu as? ChestMenu ?: return false
		if (handler.container.containerSize < 9) return false
		val middlePane = handler.container.getItem(handler.container.containerSize - 5)
		if (middlePane == null) return false
		return middlePane.displayNameAccordingToNbt?.unformattedString == "⇦ Your stuff"
	}

	fun isNpcShop(screen: AbstractContainerScreen<*>?): Boolean {
		if (screen == null) return false
		val handler = screen.menu as? ChestMenu ?: return false
		if (handler.container.containerSize < 9) return false
		val sellItem = handler.container.getItem(handler.container.containerSize - 5)
		if (sellItem == null) return false
		if (sellItem.displayNameAccordingToNbt.unformattedString == "Sell Item") return true
		val lore = sellItem.loreAccordingToNbt
		return (lore.lastOrNull() ?: return false).unformattedString == "Click to buyback!"
	}

	@Subscribe
	fun onSalvageProtect(event: IsSlotProtectedEvent) {
		if (event.slot == null) return
		if (!event.slot.hasItem()) return
		if (event.slot.item.displayNameAccordingToNbt.unformattedString != "Salvage Items") return
		val inv = event.slot.container
		var anyBlocked = false
		for (i in 0 until event.slot.containerSlot) {
			val stack = inv.getItem(i)
			if (IsSlotProtectedEvent.shouldBlockInteraction(
					null,
					ClickType.THROW,
					IsSlotProtectedEvent.MoveOrigin.SALVAGE,
					stack
				)
			)
				anyBlocked = true
		}
		if (anyBlocked) {
			event.protectSilent()
		}
	}

	@Subscribe
	fun onProtectUuidItems(event: IsSlotProtectedEvent) {
		val doesNotDeleteItem = event.actionType == ClickType.SWAP
			|| event.actionType == ClickType.PICKUP
			|| event.actionType == ClickType.QUICK_MOVE
			|| event.actionType == ClickType.QUICK_CRAFT
			|| event.actionType == ClickType.CLONE
			|| event.actionType == ClickType.PICKUP_ALL
		val isSellOrTradeScreen =
			isNpcShop(MC.handledScreen) || isTradeScreen(MC.handledScreen) || isSalvageScreen(MC.handledScreen)
		if ((!isSellOrTradeScreen || event.slot?.container !is Inventory)
			&& doesNotDeleteItem
		) return
		val stack = event.itemStack ?: return
		if (TConfig.protectAllHuntingBoxes && (stack.isHuntingBox())) {
			event.protect()
			return
		}
		val uuid = stack.skyblockUUID ?: return
		if (uuid in (lockedUUIDs ?: return)) {
			event.protect()
		}
	}

	fun ItemStack.isHuntingBox(): Boolean {
		return skyBlockId == SkyBlockItems.HUNTING_TOOLKIT || extraAttributes.get("tool_kit") != null
	}

	@Subscribe
	fun onProtectSlot(it: IsSlotProtectedEvent) {
		if (it.slot != null
			&& it.slot.container is Inventory
			&& (it.slot.containerSlot in (lockedSlots ?: setOf())
				|| (
				TConfig.lockBound &&
					currentWorldData.boundSlots.findMatchingSlots(it.slot.containerSlot).isNotEmpty())
				)
		) {
			it.protect()
		}
	}

	@Subscribe
	fun onEvent(event: ClientInitEvent) {
		IsSlotProtectedEvent.subscribe(receivesCancelled = true, "SlotLocking:unlockInDungeons") {
			if (it.isProtected
				&& it.origin == IsSlotProtectedEvent.MoveOrigin.DROP_FROM_HOTBAR
				&& DungeonUtil.isInActiveDungeon
				&& TConfig.allowDroppingInDungeons
			) {
				it.isProtected = false
			}
		}
	}

	@Subscribe
	fun onQuickMoveBoundSlot(it: IsSlotProtectedEvent) {
		val boundSlots = currentWorldData?.boundSlots ?: BoundSlots()
		val isValidAction =
			it.actionType == ClickType.QUICK_MOVE || (it.actionType == ClickType.PICKUP && !TConfig.slotBindRequireShift)
		if (!isValidAction) return
		val handler = MC.handledScreen?.menu ?: return
		if (TConfig.slotBindOnlyInInv && handler !is InventoryMenu)
			return
		val slot = it.slot
		if (slot != null && it.slot.container is Inventory) {
			val matchingSlots = boundSlots.findMatchingSlots(slot.containerSlot)
			if (matchingSlots.isEmpty()) return
			it.protectSilent()
			val boundSlot = matchingSlots.singleOrNull() ?: return
			val inventorySlot = MC.handledScreen?.getSlotByIndex(boundSlot.inventory, true)
			inventorySlot?.swapWithHotBar(handler, boundSlot.hotbar)
		}
	}

	@Subscribe
	fun onLockUUID(it: HandledScreenKeyPressedEvent) {
		if (!it.matches(TConfig.lockUUID)) return
		val inventory = MC.handledScreen ?: return
		inventory.castAccessor()

		val slot = inventory.focusedSlot_Firmament ?: return
		val stack = slot.item ?: return
		if (stack.isHuntingBox()) {
			MC.sendChat(
				tr(
					"firmament.slot-locking.hunting-box-unbindable-hint",
					"The hunting box cannot be UUID bound reliably. It changes its own UUID frequently when switching tools. "
				).red().append(
					tr(
						"firmament.slot-locking.hunting-box-unbindable-hint.solution",
						"Use the Firmament config option for locking all hunting boxes instead."
					).lime()
				)
			)
			CommonSoundEffects.playFailure()
			return
		}
		val uuid = stack.skyblockUUID ?: return
		val lockedUUIDs = lockedUUIDs ?: return
		if (uuid in lockedUUIDs) {
			lockedUUIDs.remove(uuid)
		} else {
			lockedUUIDs.add(uuid)
		}
		DConfig.markDirty()
		CommonSoundEffects.playSuccess()
		it.cancel()
	}


	@Subscribe
	fun onLockSlotKeyRelease(it: HandledScreenKeyReleasedEvent) {
		val inventory = MC.handledScreen ?: return
		inventory.castAccessor()
		val slot = inventory.focusedSlot_Firmament
		val storedSlot = storedLockingSlot ?: return

		if (it.matches(TConfig.slotBind) && slot != storedSlot && slot != null && slot.isHotbar() != storedSlot.isHotbar()) {
			storedLockingSlot = null
			val hotBarSlot = if (slot.isHotbar()) slot else storedSlot
			val invSlot = if (slot.isHotbar()) storedSlot else slot
			val boundSlots = currentWorldData?.boundSlots ?: return
			lockedSlots?.remove(hotBarSlot.containerSlot)
			lockedSlots?.remove(invSlot.containerSlot)
			boundSlots.removeDuplicateForInventory(invSlot.containerSlot)
			boundSlots.insert(hotBarSlot.containerSlot, invSlot.containerSlot)
			DConfig.markDirty()
			CommonSoundEffects.playSuccess()
			return
		}
		if (it.matches(TConfig.lockSlot) && slot == storedSlot) {
			storedLockingSlot = null
			toggleSlotLock(slot)
			return
		}
		if (it.matches(TConfig.slotBind)) {
			storedLockingSlot = null
			val boundSlots = currentWorldData?.boundSlots ?: return
			if (slot != null)
				boundSlots.removeAllInvolving(slot.containerSlot)
		}
	}

	@Subscribe
	fun onRenderAllBoundSlots(event: HandledScreenForegroundEvent) {
		val boundSlots = currentWorldData?.boundSlots ?: return
		fun findByIndex(index: Int) = event.screen.getSlotByIndex(index, true)
		val accScreen = event.screen.castAccessor()
		val sx = accScreen.x_Firmament
		val sy = accScreen.y_Firmament
		val highlitSlots = mutableSetOf<Slot>()
		for (it in boundSlots.pairs) {
			val hotbarSlot = findByIndex(it.hotbar) ?: continue
			val inventorySlot = findByIndex(it.inventory) ?: continue

			val (hotX, hotY) = hotbarSlot.lineCenter()
			val (invX, invY) = inventorySlot.lineCenter()
			val anyHovered = accScreen.focusedSlot_Firmament === hotbarSlot
				|| accScreen.focusedSlot_Firmament === inventorySlot
			if (!anyHovered && TConfig.slotRenderLines == SlotRenderLinesMode.NOTHING)
				continue
			if (anyHovered) {
				highlitSlots.add(hotbarSlot)
				highlitSlots.add(inventorySlot)
			}
			fun color(highlit: Boolean) =
				if (highlit)
					me.shedaniel.math.Color.ofOpaque(0x00FF00)
				else
					me.shedaniel.math.Color.ofTransparent(0xc0a0f000.toInt())
			if (TConfig.slotRenderLines == SlotRenderLinesMode.EVERYTHING || anyHovered)
				event.context.drawLine(
					invX + sx, invY + sy,
					hotX + sx, hotY + sy,
					color(anyHovered)
				)
			event.context.drawAlignedBox(
				hotbarSlot.x + sx,
				hotbarSlot.y + sy,
				16, 16, color(hotbarSlot in highlitSlots).color
			)
			event.context.drawAlignedBox( // TODO: 1.21.10
				inventorySlot.x + sx,
				inventorySlot.y + sy,
				16, 16, color(inventorySlot in highlitSlots).color
			)
		}
	}

	@Subscribe
	fun onRenderCurrentDraggingSlot(event: HandledScreenForegroundEvent) {
		val draggingSlot = storedLockingSlot ?: return
		val accScreen = event.screen.castAccessor()
		val hoveredSlot = accScreen.focusedSlot_Firmament
			?.takeIf { it.container is Inventory }
			?.takeIf { it == draggingSlot || it.isHotbar() != draggingSlot.isHotbar() }
		val sx = accScreen.x_Firmament
		val sy = accScreen.y_Firmament
		val (borderX, borderY) = draggingSlot.lineCenter()
		event.context.drawAlignedBox(
			draggingSlot.x + sx,
			draggingSlot.y + sy,
			16,
			16,
			0xFF00FF00u.toInt()
		) // TODO: 1.21.10
		if (hoveredSlot == null) {
			event.context.drawLine(
				borderX + sx, borderY + sy,
				event.mouseX, event.mouseY,
				me.shedaniel.math.Color.ofOpaque(0x00FF00)
			)
		} else if (hoveredSlot != draggingSlot) {
			val (hovX, hovY) = hoveredSlot.lineCenter()
			event.context.drawLine(
				borderX + sx, borderY + sy,
				hovX + sx, hovY + sy,
				me.shedaniel.math.Color.ofOpaque(0x00FF00)
			)
			event.context.drawAlignedBox(
				hoveredSlot.x + sx,
				hoveredSlot.y + sy,
				16, 16, 0xFF00FF00u.toInt()
			)
		}
	}

	fun Slot.lineCenter(): Pair<Int, Int> {
		return if (isHotbar()) {
			x + 9 to y
		} else {
			x + 9 to y + 16
		}
	}


	fun Slot.isHotbar(): Boolean {
		return containerSlot < 9
	}

	@Subscribe
	fun onScreenChange(event: ScreenChangeEvent) {
		storedLockingSlot = null
	}

	var storedLockingSlot: Slot? = null

	fun toggleSlotLock(slot: Slot) {
		val lockedSlots = lockedSlots ?: return
		val boundSlots = currentWorldData?.boundSlots ?: BoundSlots()
		if (slot.container is Inventory) {
			if (boundSlots.removeAllInvolving(slot.containerSlot)) {
				// intentionally do nothing
			} else if (slot.containerSlot in lockedSlots) {
				lockedSlots.remove(slot.containerSlot)
			} else {
				lockedSlots.add(slot.containerSlot)
			}
			DConfig.markDirty()
			CommonSoundEffects.playSuccess()
		}
	}

	@Subscribe
	fun onLockSlot(it: HandledScreenKeyPressedEvent) {
		val inventory = MC.handledScreen ?: return
		inventory.castAccessor()

		val slot = inventory.focusedSlot_Firmament ?: return
		if (slot.container !is Inventory) return
		if (it.matches(TConfig.slotBind)) {
			storedLockingSlot = storedLockingSlot ?: slot
			return
		}
		if (!it.matches(TConfig.lockSlot)) {
			return
		}
		toggleSlotLock(slot)
	}

	@Subscribe
	fun onRenderSlotOverlay(it: SlotRenderEvents.After) {
		val isSlotLocked = it.slot.container is Inventory && it.slot.containerSlot in (lockedSlots ?: setOf())
		val isUUIDLocked = (it.slot.item?.skyblockUUID) in (lockedUUIDs ?: setOf())
		if (isSlotLocked || isUUIDLocked) {
			it.context.blitSprite(
				RenderPipelines.GUI_TEXTURED,
				when {
					isSlotLocked ->
						(Identifier.parse("firmament:slot_locked"))

					isUUIDLocked ->
						(Identifier.parse("firmament:uuid_locked"))

					else ->
						error("unreachable")
				},
				it.slot.x, it.slot.y,
				16, 16,
				-1
			)
		}
	}
}
