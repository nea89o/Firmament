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
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.render.RenderLayer
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.GenericContainerScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.screen.slot.SlotActionType
import net.minecraft.util.Identifier
import net.minecraft.util.StringIdentifiable
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.FeaturesInitializedEvent
import moe.nea.firmament.events.HandledScreenForegroundEvent
import moe.nea.firmament.events.HandledScreenKeyPressedEvent
import moe.nea.firmament.events.HandledScreenKeyReleasedEvent
import moe.nea.firmament.events.IsSlotProtectedEvent
import moe.nea.firmament.events.ScreenChangeEvent
import moe.nea.firmament.events.SlotRenderEvents
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.keybindings.SavedKeyBinding
import moe.nea.firmament.mixins.accessor.AccessorHandledScreen
import moe.nea.firmament.util.CommonSoundEffects
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.SkyBlockIsland
import moe.nea.firmament.util.data.ProfileSpecificDataHolder
import moe.nea.firmament.util.extraAttributes
import moe.nea.firmament.util.json.DashlessUUIDSerializer
import moe.nea.firmament.util.lime
import moe.nea.firmament.util.mc.ScreenUtil.getSlotByIndex
import moe.nea.firmament.util.mc.SlotUtils.swapWithHotBar
import moe.nea.firmament.util.mc.displayNameAccordingToNbt
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.red
import moe.nea.firmament.util.render.drawLine
import moe.nea.firmament.util.skyBlockId
import moe.nea.firmament.util.skyblock.DungeonUtil
import moe.nea.firmament.util.skyblock.SkyBlockItems
import moe.nea.firmament.util.skyblockUUID
import moe.nea.firmament.util.tr
import moe.nea.firmament.util.unformattedString

object SlotLocking : FirmamentFeature {
	override val identifier: String
		get() = "slot-locking"

	@Serializable
	data class Data(
		val lockedSlots: MutableSet<Int> = mutableSetOf(),
		val lockedSlotsRift: MutableSet<Int> = mutableSetOf(),
		val lockedUUIDs: MutableSet<UUID> = mutableSetOf(),
		val boundSlots: BoundSlots = BoundSlots()
	)

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


	object TConfig : ManagedConfig(identifier, Category.INVENTORY) {
		val lockSlot by keyBinding("lock") { GLFW.GLFW_KEY_L }
		val lockUUID by keyBindingWithOutDefaultModifiers("lock-uuid") {
			SavedKeyBinding(GLFW.GLFW_KEY_L, shift = true)
		}
		val slotBind by keyBinding("bind") { GLFW.GLFW_KEY_L }
		val slotBindRequireShift by toggle("require-quick-move") { true }
		val slotRenderLines by choice("bind-render") { SlotRenderLinesMode.ONLY_BOXES }
		val allowMultiBinding by toggle("multi-bind") { true } // TODO: filter based on this option
		val protectAllHuntingBoxes by toggle("hunting-box") { false }
		val allowDroppingInDungeons by toggle("drop-in-dungeons") { true }
	}

	enum class SlotRenderLinesMode : StringIdentifiable {
		EVERYTHING,
		ONLY_BOXES,
		NOTHING;

		override fun asString(): String {
			return name
		}
	}

	override val config: TConfig
		get() = TConfig

	object DConfig : ProfileSpecificDataHolder<Data>(serializer(), "locked-slots", ::Data)

	val lockedUUIDs get() = DConfig.data?.lockedUUIDs

	val lockedSlots
		get() = when (SBData.skyblockLocation) {
			SkyBlockIsland.RIFT -> DConfig.data?.lockedSlotsRift
			null -> null
			else -> DConfig.data?.lockedSlots
		}

	fun isSalvageScreen(screen: HandledScreen<*>?): Boolean {
		if (screen == null) return false
		return screen.title.unformattedString.contains("Salvage Item")
	}

	fun isTradeScreen(screen: HandledScreen<*>?): Boolean {
		if (screen == null) return false
		val handler = screen.screenHandler as? GenericContainerScreenHandler ?: return false
		if (handler.inventory.size() < 9) return false
		val middlePane = handler.inventory.getStack(handler.inventory.size() - 5)
		if (middlePane == null) return false
		return middlePane.displayNameAccordingToNbt?.unformattedString == "⇦ Your stuff"
	}

	fun isNpcShop(screen: HandledScreen<*>?): Boolean {
		if (screen == null) return false
		val handler = screen.screenHandler as? GenericContainerScreenHandler ?: return false
		if (handler.inventory.size() < 9) return false
		val sellItem = handler.inventory.getStack(handler.inventory.size() - 5)
		if (sellItem == null) return false
		if (sellItem.displayNameAccordingToNbt.unformattedString == "Sell Item") return true
		val lore = sellItem.loreAccordingToNbt
		return (lore.lastOrNull() ?: return false).unformattedString == "Click to buyback!"
	}

	@Subscribe
	fun onSalvageProtect(event: IsSlotProtectedEvent) {
		if (event.slot == null) return
		if (!event.slot.hasStack()) return
		if (event.slot.stack.displayNameAccordingToNbt.unformattedString != "Salvage Items") return
		val inv = event.slot.inventory
		var anyBlocked = false
		for (i in 0 until event.slot.index) {
			val stack = inv.getStack(i)
			if (IsSlotProtectedEvent.shouldBlockInteraction(
					null,
					SlotActionType.THROW,
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
		val doesNotDeleteItem = event.actionType == SlotActionType.SWAP
			|| event.actionType == SlotActionType.PICKUP
			|| event.actionType == SlotActionType.QUICK_MOVE
			|| event.actionType == SlotActionType.QUICK_CRAFT
			|| event.actionType == SlotActionType.CLONE
			|| event.actionType == SlotActionType.PICKUP_ALL
		val isSellOrTradeScreen =
			isNpcShop(MC.handledScreen) || isTradeScreen(MC.handledScreen) || isSalvageScreen(MC.handledScreen)
		if ((!isSellOrTradeScreen || event.slot?.inventory !is PlayerInventory)
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
		if (it.slot != null && it.slot.inventory is PlayerInventory && it.slot.index in (lockedSlots ?: setOf())) {
			it.protect()
		}
	}

	@Subscribe
	fun onEvent(event: FeaturesInitializedEvent) {
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
		val boundSlots = DConfig.data?.boundSlots ?: BoundSlots()
		val isValidAction =
			it.actionType == SlotActionType.QUICK_MOVE || (it.actionType == SlotActionType.PICKUP && !TConfig.slotBindRequireShift)
		if (!isValidAction) return
		val handler = MC.handledScreen?.screenHandler ?: return
		val slot = it.slot
		if (slot != null && it.slot.inventory is PlayerInventory) {
			val matchingSlots = boundSlots.findMatchingSlots(slot.index)
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
		inventory as AccessorHandledScreen

		val slot = inventory.focusedSlot_Firmament ?: return
		val stack = slot.stack ?: return
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
		inventory as AccessorHandledScreen
		val slot = inventory.focusedSlot_Firmament
		val storedSlot = storedLockingSlot ?: return

		if (it.matches(TConfig.slotBind) && slot != storedSlot && slot != null && slot.isHotbar() != storedSlot.isHotbar()) {
			storedLockingSlot = null
			val hotBarSlot = if (slot.isHotbar()) slot else storedSlot
			val invSlot = if (slot.isHotbar()) storedSlot else slot
			val boundSlots = DConfig.data?.boundSlots ?: return
			lockedSlots?.remove(hotBarSlot.index)
			lockedSlots?.remove(invSlot.index)
			boundSlots.removeDuplicateForInventory(invSlot.index)
			boundSlots.insert(hotBarSlot.index, invSlot.index)
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
			val boundSlots = DConfig.data?.boundSlots ?: return
			if (slot != null)
				boundSlots.removeAllInvolving(slot.index)
		}
	}

	@Subscribe
	fun onRenderAllBoundSlots(event: HandledScreenForegroundEvent) {
		val boundSlots = DConfig.data?.boundSlots ?: return
		fun findByIndex(index: Int) = event.screen.getSlotByIndex(index, true)
		val accScreen = event.screen as AccessorHandledScreen
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
			event.context.drawBorder(
				hotbarSlot.x + sx,
				hotbarSlot.y + sy,
				16, 16, color(hotbarSlot in highlitSlots).color
			)
			event.context.drawBorder(
				inventorySlot.x + sx,
				inventorySlot.y + sy,
				16, 16, color(inventorySlot in highlitSlots).color
			)
		}
	}

	@Subscribe
	fun onRenderCurrentDraggingSlot(event: HandledScreenForegroundEvent) {
		val draggingSlot = storedLockingSlot ?: return
		val accScreen = event.screen as AccessorHandledScreen
		val hoveredSlot = accScreen.focusedSlot_Firmament
			?.takeIf { it.inventory is PlayerInventory }
			?.takeIf { it == draggingSlot || it.isHotbar() != draggingSlot.isHotbar() }
		val sx = accScreen.x_Firmament
		val sy = accScreen.y_Firmament
		val (borderX, borderY) = draggingSlot.lineCenter()
		event.context.drawBorder(draggingSlot.x + sx, draggingSlot.y + sy, 16, 16, 0xFF00FF00u.toInt())
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
			event.context.drawBorder(
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
			x + 9 to y + 17
		}
	}


	fun Slot.isHotbar(): Boolean {
		return index < 9
	}

	@Subscribe
	fun onScreenChange(event: ScreenChangeEvent) {
		storedLockingSlot = null
	}

	var storedLockingSlot: Slot? = null

	fun toggleSlotLock(slot: Slot) {
		val lockedSlots = lockedSlots ?: return
		val boundSlots = DConfig.data?.boundSlots ?: BoundSlots()
		if (slot.inventory is PlayerInventory) {
			if (boundSlots.removeAllInvolving(slot.index)) {
				// intentionally do nothing
			} else if (slot.index in lockedSlots) {
				lockedSlots.remove(slot.index)
			} else {
				lockedSlots.add(slot.index)
			}
			DConfig.markDirty()
			CommonSoundEffects.playSuccess()
		}
	}

	@Subscribe
	fun onLockSlot(it: HandledScreenKeyPressedEvent) {
		val inventory = MC.handledScreen ?: return
		inventory as AccessorHandledScreen

		val slot = inventory.focusedSlot_Firmament ?: return
		if (slot.inventory !is PlayerInventory) return
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
		val isSlotLocked = it.slot.inventory is PlayerInventory && it.slot.index in (lockedSlots ?: setOf())
		val isUUIDLocked = (it.slot.stack?.skyblockUUID) in (lockedUUIDs ?: setOf())
		if (isSlotLocked || isUUIDLocked) {
			it.context.drawGuiTexture(
				RenderLayer::getGuiTexturedOverlay,
				when {
					isSlotLocked ->
						(Identifier.of("firmament:slot_locked"))

					isUUIDLocked ->
						(Identifier.of("firmament:uuid_locked"))

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
