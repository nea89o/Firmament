package moe.nea.firmament.features.debug

import com.mojang.serialization.JsonOps
import kotlin.jvm.optionals.getOrNull
import net.minecraft.advancements.criterion.NbtPredicate
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.resources.Identifier
import net.minecraft.world.Nameable
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ResolvableProfile
import net.minecraft.world.level.block.SkullBlock
import net.minecraft.world.level.block.entity.SkullBlockEntity
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.CustomItemModelEvent
import moe.nea.firmament.events.HandledScreenKeyPressedEvent
import moe.nea.firmament.events.ItemTooltipEvent
import moe.nea.firmament.events.ScreenChangeEvent
import moe.nea.firmament.events.SlotRenderEvents
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.events.WorldKeyboardEvent
import moe.nea.firmament.mixins.accessor.AccessorHandledScreen
import moe.nea.firmament.util.ClipboardUtils
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.data.Config
import moe.nea.firmament.util.data.ManagedConfig
import moe.nea.firmament.util.focusedItemStack
import moe.nea.firmament.util.grey
import moe.nea.firmament.util.mc.IntrospectableItemModelManager
import moe.nea.firmament.util.mc.SNbtFormatter
import moe.nea.firmament.util.mc.SNbtFormatter.Companion.toPrettyString
import moe.nea.firmament.util.mc.displayNameAccordingToNbt
import moe.nea.firmament.util.mc.iterableArmorItems
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.mc.unsafeNbt
import moe.nea.firmament.util.skyBlockId
import moe.nea.firmament.util.tr

object PowerUserTools {
	val identifier: String
		get() = "power-user"

	@Config
	object TConfig : ManagedConfig(identifier, Category.DEV) {
		val showItemIds by toggle("show-item-id") { false }
		val copyItemId by keyBindingWithDefaultUnbound("copy-item-id")
		val copyTexturePackId by keyBindingWithDefaultUnbound("copy-texture-pack-id")
		val copyNbtData by keyBindingWithDefaultUnbound("copy-nbt-data")
		val copyLoreData by keyBindingWithDefaultUnbound("copy-lore")
		val copySkullTexture by keyBindingWithDefaultUnbound("copy-skull-texture")
		val copyEntityData by keyBindingWithDefaultUnbound("entity-data")
		val copyItemStack by keyBindingWithDefaultUnbound("copy-item-stack")
		val copyTitle by keyBindingWithDefaultUnbound("copy-title")
		val exportItemStackToRepo by keyBindingWithDefaultUnbound("export-item-stack")
		val exportUIRecipes by keyBindingWithDefaultUnbound("export-recipe")
		val exportNpcLocation by keyBindingWithDefaultUnbound("export-npc-location")
		val highlightNonOverlayItems by toggle("highlight-non-overlay") { false }
		val dontHighlightSemicolonItems by toggle("dont-highlight-semicolon-items") { false }
		val showSlotNumbers by keyBindingWithDefaultUnbound("slot-numbers")
		val autoCopyAnimatedSkins by toggle("copy-animated-skins") { false }
	}

	var lastCopiedStack: Pair<ItemStack, Component>? = null
		set(value) {
			field = value
			if (value != null) lastCopiedStackViewTime = 2
		}
	var lastCopiedStackViewTime = 0

	@Subscribe
	fun resetLastCopiedStack(event: TickEvent) {
		if (lastCopiedStackViewTime-- < 0) lastCopiedStack = null
	}

	@Subscribe
	fun resetLastCopiedStackOnScreenChange(event: ScreenChangeEvent) {
		lastCopiedStack = null
	}

	fun debugFormat(itemStack: ItemStack): Component {
		return Component.literal(itemStack.skyBlockId?.toString() ?: itemStack.toString())
	}

	@Subscribe
	fun onRender(event: SlotRenderEvents.After) {
		if (TConfig.showSlotNumbers.isPressed()) {
			event.context.drawString(
				MC.font,
				event.slot.index.toString(), event.slot.x, event.slot.y, 0xFF00FF00.toInt(), true
			)
			event.context.drawString(
				MC.font,
				event.slot.containerSlot.toString(), event.slot.x, event.slot.y + MC.font.lineHeight, 0xFFFF0000.toInt(), true
			)
		}
	}

	@Subscribe
	fun onEntityInfo(event: WorldKeyboardEvent) {
		if (!event.matches(TConfig.copyEntityData)) return
		val target = (MC.instance.hitResult as? EntityHitResult)?.entity
		if (target == null) {
			MC.sendChat(Component.translatable("firmament.poweruser.entity.fail"))
			return
		}
		showEntity(target)
	}

	fun showEntity(target: Entity) {
		val nbt = NbtPredicate.getEntityTagToCompare(target)
		nbt.remove("Inventory")
		nbt.put("StyledName", ComponentSerialization.CODEC.encodeStart(NbtOps.INSTANCE, target.feedbackDisplayName).orThrow)
		println(SNbtFormatter.prettify(nbt))
		ClipboardUtils.setTextContent(SNbtFormatter.prettify(nbt))
		MC.sendChat(Component.translatable("firmament.poweruser.entity.type", target.type))
		MC.sendChat(Component.translatable("firmament.poweruser.entity.name", target.name))
		MC.sendChat(Component.translatableEscape("firmament.poweruser.entity.position", target.position))
		if (target is LivingEntity) {
			MC.sendChat(Component.translatable("firmament.poweruser.entity.armor"))
			for ((slot, armorItem) in target.iterableArmorItems) {
				MC.sendChat(Component.translatable("firmament.poweruser.entity.armor.item", debugFormat(armorItem)))
			}
		}
		MC.sendChat(Component.translatableEscape("firmament.poweruser.entity.passengers", target.passengers.size))
		target.passengers.forEach {
			showEntity(it)
		}
	}

	// TODO: leak this through some other way, maybe.
	lateinit var getSkullId: (profile: ResolvableProfile) -> Identifier?

	@Subscribe
	fun copyInventoryInfo(it: HandledScreenKeyPressedEvent) {
		if (it.screen !is AccessorHandledScreen) return
		val item = it.screen.focusedItemStack ?: return
		if (it.matches(TConfig.copyItemId)) {
			val sbId = item.skyBlockId
			if (sbId == null) {
				lastCopiedStack = Pair(item, Component.translatable("firmament.tooltip.copied.skyblockid.fail"))
				return
			}
			ClipboardUtils.setTextContent(sbId.neuItem)
			lastCopiedStack =
				Pair(item, Component.translatableEscape("firmament.tooltip.copied.skyblockid", sbId.neuItem))
		} else if (it.matches(TConfig.copyTexturePackId)) {
			val model = CustomItemModelEvent.getModelIdentifier0(item, object : IntrospectableItemModelManager {
				override fun hasModel_firmament(identifier: Identifier): Boolean {
					return true
				}
			}).getOrNull() // TODO: remove global texture overrides, maybe
			if (model == null) {
				lastCopiedStack = Pair(item, Component.translatable("firmament.tooltip.copied.modelid.fail"))
				return
			}
			ClipboardUtils.setTextContent(model.toString())
			lastCopiedStack =
				Pair(item, Component.translatableEscape("firmament.tooltip.copied.modelid", model.toString()))
		} else if (it.matches(TConfig.copyNbtData)) {
			// TODO: copy full nbt
			val nbt = item.get(DataComponents.CUSTOM_DATA)?.unsafeNbt?.toPrettyString() ?: "<empty>"
			ClipboardUtils.setTextContent(nbt)
			lastCopiedStack = Pair(item, Component.translatable("firmament.tooltip.copied.nbt"))
		} else if (it.matches(TConfig.copyLoreData)) {
			val list = mutableListOf(item.displayNameAccordingToNbt)
			list.addAll(item.loreAccordingToNbt)
			ClipboardUtils.setTextContent(list.joinToString("\n") {
				ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, it).result().getOrNull().toString()
			})
			lastCopiedStack = Pair(item, Component.translatable("firmament.tooltip.copied.lore"))
		} else if (it.matches(TConfig.copySkullTexture)) {
			if (item.item != Items.PLAYER_HEAD) {
				lastCopiedStack = Pair(item, Component.translatable("firmament.tooltip.copied.skull-id.fail.no-skull"))
				return
			}
			val profile = item.get(DataComponents.PROFILE)
			if (profile == null) {
				lastCopiedStack = Pair(item, Component.translatable("firmament.tooltip.copied.skull-id.fail.no-profile"))
				return
			}
			val skullTexture = getSkullId(profile)
			if (skullTexture == null) {
				lastCopiedStack = Pair(item, Component.translatable("firmament.tooltip.copied.skull-id.fail.no-texture"))
				return
			}
			ClipboardUtils.setTextContent(skullTexture.toString())
			lastCopiedStack =
				Pair(item, Component.translatableEscape("firmament.tooltip.copied.skull-id", skullTexture.toString()))
			println("Copied skull id: $skullTexture")
		} else if (it.matches(TConfig.copyItemStack)) {
			val nbt = ItemStack.CODEC
				.encodeStart(MC.currentOrDefaultRegistries.createSerializationContext(NbtOps.INSTANCE), item)
				.orThrow
			ClipboardUtils.setTextContent(nbt.toPrettyString())
			lastCopiedStack = Pair(item, Component.translatableEscape("firmament.tooltip.copied.stack"))
		} else if (it.matches(TConfig.copyTitle) && it.screen is AbstractContainerScreen<*>) {
			val allTitles = ListTag()
			val inventoryNames =
				it.screen.menu.slots
					.mapNotNullTo(mutableSetOf()) { it.container }
					.filterIsInstance<Nameable>()
					.map { it.name }
			for (it in listOf(it.screen.title) + inventoryNames) {
				allTitles.add(ComponentSerialization.CODEC.encodeStart(NbtOps.INSTANCE, it).result().getOrNull()!!)
			}
			ClipboardUtils.setTextContent(allTitles.toPrettyString())
			MC.sendChat(tr("firmament.power-user.title.copied", "Copied screen and inventory titles"))
		}
	}

	@Subscribe
	fun onCopyWorldInfo(it: WorldKeyboardEvent) {
		if (it.matches(TConfig.copySkullTexture)) {
			val p = MC.camera ?: return
			val blockHit = p.pick(20.0, 0.0f, false) ?: return
			if (blockHit.type != HitResult.Type.BLOCK || blockHit !is BlockHitResult) {
				MC.sendChat(Component.translatable("firmament.tooltip.copied.skull.fail"))
				return
			}
			val blockAt = p.level.getBlockState(blockHit.blockPos)?.block
			val entity = p.level.getBlockEntity(blockHit.blockPos)
			if (blockAt !is SkullBlock || entity !is SkullBlockEntity || entity.ownerProfile == null) {
				MC.sendChat(Component.translatable("firmament.tooltip.copied.skull.fail"))
				return
			}
			val id = getSkullId(entity.ownerProfile!!)
			if (id == null) {
				MC.sendChat(Component.translatable("firmament.tooltip.copied.skull.fail"))
			} else {
				ClipboardUtils.setTextContent(id.toString())
				MC.sendChat(Component.translatableEscape("firmament.tooltip.copied.skull", id.toString()))
			}
		}
	}

	@Subscribe
	fun addItemId(it: ItemTooltipEvent) {
		if (TConfig.showItemIds) {
			val id = it.stack.skyBlockId ?: return
			it.lines.add(Component.translatableEscape("firmament.tooltip.skyblockid", id.neuItem).grey())
		}
		val (item, text) = lastCopiedStack ?: return
		if (!ItemStack.matches(item, it.stack)) {
			lastCopiedStack = null
			return
		}
		lastCopiedStackViewTime = 0
		it.lines.add(text)
	}


}
