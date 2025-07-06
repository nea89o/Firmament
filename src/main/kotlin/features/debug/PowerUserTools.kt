package moe.nea.firmament.features.debug

import com.mojang.serialization.JsonOps
import kotlin.jvm.optionals.getOrNull
import net.minecraft.block.SkullBlock
import net.minecraft.block.entity.SkullBlockEntity
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.ProfileComponent
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtOps
import net.minecraft.predicate.NbtPredicate
import net.minecraft.text.Text
import net.minecraft.text.TextCodecs
import net.minecraft.util.Identifier
import net.minecraft.util.Nameable
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.CustomItemModelEvent
import moe.nea.firmament.events.HandledScreenKeyPressedEvent
import moe.nea.firmament.events.ItemTooltipEvent
import moe.nea.firmament.events.ScreenChangeEvent
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.events.WorldKeyboardEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.mixins.accessor.AccessorHandledScreen
import moe.nea.firmament.util.ClipboardUtils
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.focusedItemStack
import moe.nea.firmament.util.mc.IntrospectableItemModelManager
import moe.nea.firmament.util.mc.SNbtFormatter
import moe.nea.firmament.util.mc.SNbtFormatter.Companion.toPrettyString
import moe.nea.firmament.util.mc.displayNameAccordingToNbt
import moe.nea.firmament.util.mc.iterableArmorItems
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.skyBlockId
import moe.nea.firmament.util.tr
import moe.nea.firmament.util.grey

object PowerUserTools : FirmamentFeature {
	override val identifier: String
		get() = "power-user"

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
	}

	override val config
		get() = TConfig

	var lastCopiedStack: Pair<ItemStack, Text>? = null
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

	fun debugFormat(itemStack: ItemStack): Text {
		return Text.literal(itemStack.skyBlockId?.toString() ?: itemStack.toString())
	}

	@Subscribe
	fun onEntityInfo(event: WorldKeyboardEvent) {
		if (!event.matches(TConfig.copyEntityData)) return
		val target = (MC.instance.crosshairTarget as? EntityHitResult)?.entity
		if (target == null) {
			MC.sendChat(Text.translatable("firmament.poweruser.entity.fail"))
			return
		}
		showEntity(target)
	}

	fun showEntity(target: Entity) {
		val nbt = NbtPredicate.entityToNbt(target)
		nbt.remove("Inventory")
		nbt.put("StyledName", TextCodecs.CODEC.encodeStart(NbtOps.INSTANCE, target.styledDisplayName).orThrow)
		println(SNbtFormatter.prettify(nbt))
		ClipboardUtils.setTextContent(SNbtFormatter.prettify(nbt))
		MC.sendChat(Text.translatable("firmament.poweruser.entity.type", target.type))
		MC.sendChat(Text.translatable("firmament.poweruser.entity.name", target.name))
		MC.sendChat(Text.stringifiedTranslatable("firmament.poweruser.entity.position", target.pos))
		if (target is LivingEntity) {
			MC.sendChat(Text.translatable("firmament.poweruser.entity.armor"))
			for ((slot, armorItem) in target.iterableArmorItems) {
				MC.sendChat(Text.translatable("firmament.poweruser.entity.armor.item", debugFormat(armorItem)))
			}
		}
		MC.sendChat(Text.stringifiedTranslatable("firmament.poweruser.entity.passengers", target.passengerList.size))
		target.passengerList.forEach {
			showEntity(it)
		}
	}

	// TODO: leak this through some other way, maybe.
	lateinit var getSkullId: (profile: ProfileComponent) -> Identifier?

	@Subscribe
	fun copyInventoryInfo(it: HandledScreenKeyPressedEvent) {
		if (it.screen !is AccessorHandledScreen) return
		val item = it.screen.focusedItemStack ?: return
		if (it.matches(TConfig.copyItemId)) {
			val sbId = item.skyBlockId
			if (sbId == null) {
				lastCopiedStack = Pair(item, Text.translatable("firmament.tooltip.copied.skyblockid.fail"))
				return
			}
			ClipboardUtils.setTextContent(sbId.neuItem)
			lastCopiedStack =
				Pair(item, Text.stringifiedTranslatable("firmament.tooltip.copied.skyblockid", sbId.neuItem))
		} else if (it.matches(TConfig.copyTexturePackId)) {
			val model = CustomItemModelEvent.getModelIdentifier0(item, object : IntrospectableItemModelManager {
				override fun hasModel_firmament(identifier: Identifier): Boolean {
					return true
				}
			}).getOrNull() // TODO: remove global texture overrides, maybe
			if (model == null) {
				lastCopiedStack = Pair(item, Text.translatable("firmament.tooltip.copied.modelid.fail"))
				return
			}
			ClipboardUtils.setTextContent(model.toString())
			lastCopiedStack =
				Pair(item, Text.stringifiedTranslatable("firmament.tooltip.copied.modelid", model.toString()))
		} else if (it.matches(TConfig.copyNbtData)) {
			// TODO: copy full nbt
			val nbt = item.get(DataComponentTypes.CUSTOM_DATA)?.nbt?.toPrettyString() ?: "<empty>"
			ClipboardUtils.setTextContent(nbt)
			lastCopiedStack = Pair(item, Text.translatable("firmament.tooltip.copied.nbt"))
		} else if (it.matches(TConfig.copyLoreData)) {
			val list = mutableListOf(item.displayNameAccordingToNbt)
			list.addAll(item.loreAccordingToNbt)
			ClipboardUtils.setTextContent(list.joinToString("\n") {
				TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, it).result().getOrNull().toString()
			})
			lastCopiedStack = Pair(item, Text.translatable("firmament.tooltip.copied.lore"))
		} else if (it.matches(TConfig.copySkullTexture)) {
			if (item.item != Items.PLAYER_HEAD) {
				lastCopiedStack = Pair(item, Text.translatable("firmament.tooltip.copied.skull-id.fail.no-skull"))
				return
			}
			val profile = item.get(DataComponentTypes.PROFILE)
			if (profile == null) {
				lastCopiedStack = Pair(item, Text.translatable("firmament.tooltip.copied.skull-id.fail.no-profile"))
				return
			}
			val skullTexture = getSkullId(profile)
			if (skullTexture == null) {
				lastCopiedStack = Pair(item, Text.translatable("firmament.tooltip.copied.skull-id.fail.no-texture"))
				return
			}
			ClipboardUtils.setTextContent(skullTexture.toString())
			lastCopiedStack =
				Pair(item, Text.stringifiedTranslatable("firmament.tooltip.copied.skull-id", skullTexture.toString()))
			println("Copied skull id: $skullTexture")
		} else if (it.matches(TConfig.copyItemStack)) {
			val nbt = ItemStack.CODEC
				.encodeStart(MC.currentOrDefaultRegistries.getOps(NbtOps.INSTANCE), item)
				.orThrow
			ClipboardUtils.setTextContent(nbt.toPrettyString())
			lastCopiedStack = Pair(item, Text.stringifiedTranslatable("firmament.tooltip.copied.stack"))
		} else if (it.matches(TConfig.copyTitle)) {
			val allTitles = NbtList()
			val inventoryNames =
				it.screen.screenHandler.slots
					.mapNotNullTo(mutableSetOf()) { it.inventory }
					.filterIsInstance<Nameable>()
					.map { it.name }
			for (it in listOf(it.screen.title) + inventoryNames) {
				allTitles.add(TextCodecs.CODEC.encodeStart(NbtOps.INSTANCE, it).result().getOrNull()!!)
			}
			ClipboardUtils.setTextContent(allTitles.toPrettyString())
			MC.sendChat(tr("firmament.power-user.title.copied", "Copied screen and inventory titles"))
		}
	}

	@Subscribe
	fun onCopyWorldInfo(it: WorldKeyboardEvent) {
		if (it.matches(TConfig.copySkullTexture)) {
			val p = MC.camera ?: return
			val blockHit = p.raycast(20.0, 0.0f, false) ?: return
			if (blockHit.type != HitResult.Type.BLOCK || blockHit !is BlockHitResult) {
				MC.sendChat(Text.translatable("firmament.tooltip.copied.skull.fail"))
				return
			}
			val blockAt = p.world.getBlockState(blockHit.blockPos)?.block
			val entity = p.world.getBlockEntity(blockHit.blockPos)
			if (blockAt !is SkullBlock || entity !is SkullBlockEntity || entity.owner == null) {
				MC.sendChat(Text.translatable("firmament.tooltip.copied.skull.fail"))
				return
			}
			val id = getSkullId(entity.owner!!)
			if (id == null) {
				MC.sendChat(Text.translatable("firmament.tooltip.copied.skull.fail"))
			} else {
				ClipboardUtils.setTextContent(id.toString())
				MC.sendChat(Text.stringifiedTranslatable("firmament.tooltip.copied.skull", id.toString()))
			}
		}
	}

	@Subscribe
	fun addItemId(it: ItemTooltipEvent) {
		if (TConfig.showItemIds) {
			val id = it.stack.skyBlockId ?: return
			it.lines.add(Text.stringifiedTranslatable("firmament.tooltip.skyblockid", id.neuItem).grey())
		}
		val (item, text) = lastCopiedStack ?: return
		if (!ItemStack.areEqual(item, it.stack)) {
			lastCopiedStack = null
			return
		}
		lastCopiedStackViewTime = 0
		it.lines.add(text)
	}


}
