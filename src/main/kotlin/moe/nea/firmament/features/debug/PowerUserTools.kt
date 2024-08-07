

package moe.nea.firmament.features.debug

import net.minecraft.block.SkullBlock
import net.minecraft.block.entity.SkullBlockEntity
import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Text
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.CommandEvent
import moe.nea.firmament.events.CustomItemModelEvent
import moe.nea.firmament.events.HandledScreenKeyPressedEvent
import moe.nea.firmament.events.ItemTooltipEvent
import moe.nea.firmament.events.ScreenChangeEvent
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.events.WorldKeyboardEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.features.texturepack.CustomSkyBlockTextures
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.mixins.accessor.AccessorHandledScreen
import moe.nea.firmament.util.ClipboardUtils
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.focusedItemStack
import moe.nea.firmament.util.skyBlockId

object PowerUserTools : FirmamentFeature {
    override val identifier: String
        get() = "power-user"

    object TConfig : ManagedConfig(identifier) {
        val showItemIds by toggle("show-item-id") { false }
        val copyItemId by keyBindingWithDefaultUnbound("copy-item-id")
        val copyTexturePackId by keyBindingWithDefaultUnbound("copy-texture-pack-id")
        val copyNbtData by keyBindingWithDefaultUnbound("copy-nbt-data")
        val copySkullTexture by keyBindingWithDefaultUnbound("copy-skull-texture")
        val copyEntityData by keyBindingWithDefaultUnbound("entity-data")
    }

    override val config
        get() = TConfig

    var lastCopiedStack: Pair<ItemStack, Text>? = null
        set(value) {
            field = value
            if (value != null)
                lastCopiedStackViewTime = true
        }
    var lastCopiedStackViewTime = false

    override fun onLoad() {
        TickEvent.subscribe {
            if (!lastCopiedStackViewTime)
                lastCopiedStack = null
            lastCopiedStackViewTime = false
        }
        ScreenChangeEvent.subscribe {
            lastCopiedStack = null
        }
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
        MC.sendChat(Text.translatable("firmament.poweruser.entity.type", target.type))
        MC.sendChat(Text.translatable("firmament.poweruser.entity.name", target.name))
        MC.sendChat(Text.stringifiedTranslatable("firmament.poweruser.entity.position", target.pos))
        if (target is LivingEntity) {
            MC.sendChat(Text.translatable("firmament.poweruser.entity.armor"))
            for (armorItem in target.armorItems) {
                MC.sendChat(Text.translatable("firmament.poweruser.entity.armor.item", debugFormat(armorItem)))
            }
        }
        MC.sendChat(Text.stringifiedTranslatable("firmament.poweruser.entity.passengers", target.passengerList.size))
        target.passengerList.forEach {
            showEntity(it)
        }
    }


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
            val model = CustomItemModelEvent.getModelIdentifier(item)
            if (model == null) {
                lastCopiedStack = Pair(item, Text.translatable("firmament.tooltip.copied.modelid.fail"))
                return
            }
            ClipboardUtils.setTextContent(model.toString())
            lastCopiedStack =
                Pair(item, Text.stringifiedTranslatable("firmament.tooltip.copied.modelid", model.toString()))
        } else if (it.matches(TConfig.copyNbtData)) {
            // TODO: copy full nbt
            val nbt = item.get(DataComponentTypes.CUSTOM_DATA)?.nbt?.toString() ?: "<empty>"
            ClipboardUtils.setTextContent(nbt)
            lastCopiedStack = Pair(item, Text.translatable("firmament.tooltip.copied.nbt"))
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
            val skullTexture = CustomSkyBlockTextures.getSkullTexture(profile)
            if (skullTexture == null) {
                lastCopiedStack = Pair(item, Text.translatable("firmament.tooltip.copied.skull-id.fail.no-texture"))
                return
            }
            ClipboardUtils.setTextContent(skullTexture.toString())
            lastCopiedStack =
                Pair(
                    item,
                    Text.stringifiedTranslatable("firmament.tooltip.copied.skull-id", skullTexture.toString())
                )
            println("Copied skull id: $skullTexture")
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
            val id = CustomSkyBlockTextures.getSkullTexture(entity.owner!!)
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
            it.lines.add(Text.stringifiedTranslatable("firmament.tooltip.skyblockid", id.neuItem))
        }
        val (item, text) = lastCopiedStack ?: return
        if (!ItemStack.areEqual(item, it.stack)) {
            lastCopiedStack = null
            return
        }
        lastCopiedStackViewTime = true
        it.lines.add(text)
    }


}
