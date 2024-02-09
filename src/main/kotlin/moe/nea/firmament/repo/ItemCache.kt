/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.repo

import com.mojang.authlib.GameProfile
import com.mojang.authlib.minecraft.MinecraftProfileTexture
import com.mojang.serialization.Dynamic
import io.github.cottonmc.cotton.gui.client.CottonHud
import io.github.moulberry.repo.IReloadable
import io.github.moulberry.repo.NEURepository
import io.github.moulberry.repo.data.NEUItem
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import org.apache.logging.log4j.LogManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.minecraft.SharedConstants
import net.minecraft.block.entity.SkullBlockEntity
import net.minecraft.client.resource.language.I18n
import net.minecraft.datafixer.Schemas
import net.minecraft.datafixer.TypeReferences
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.NbtString
import net.minecraft.text.Text
import moe.nea.firmament.Firmament
import moe.nea.firmament.util.LegacyTagParser
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.appendLore
import moe.nea.firmament.util.getOrCreateList
import moe.nea.firmament.util.item.MinecraftProfileTextureKt
import moe.nea.firmament.util.item.MinecraftTexturesPayloadKt
import moe.nea.firmament.util.item.setTextures
import moe.nea.firmament.util.skyblockId

object ItemCache : IReloadable {
    private val cache: MutableMap<String, ItemStack> = ConcurrentHashMap()
    private val df = Schemas.getFixer()
    val logger = LogManager.getLogger("${Firmament.logger.name}.ItemCache")
    var isFlawless = true
        private set

    private fun NEUItem.get10809CompoundTag(): NbtCompound = NbtCompound().apply {
        put("tag", LegacyTagParser.parse(nbttag))
        putString("id", minecraftItemId)
        putByte("Count", 1)
        putShort("Damage", damage.toShort())
    }

    private fun NbtCompound.transformFrom10809ToModern(): NbtCompound? =
        try {
            df.update(
                TypeReferences.ITEM_STACK,
                Dynamic(NbtOps.INSTANCE, this),
                -1,
                SharedConstants.getGameVersion().saveVersion.id
            ).value as NbtCompound
        } catch (e: Exception) {
            isFlawless = false
            logger.error("Could not data fix up $this", e)
            null
        }

    fun brokenItemStack(neuItem: NEUItem?, idHint: SkyblockId? = null): ItemStack {
        return ItemStack(Items.PAINTING).apply {
            setCustomName(Text.literal(neuItem?.displayName ?: idHint?.neuItem ?: "null"))
            appendLore(
                listOf(
                    Text.stringifiedTranslatable(
                        "firmament.repo.brokenitem",
                        (neuItem?.skyblockItemId ?: idHint)
                    )
                )
            )
        }
    }

    private fun NEUItem.asItemStackNow(): ItemStack {
        try {
            val oldItemTag = get10809CompoundTag()
            val modernItemTag = oldItemTag.transformFrom10809ToModern()
                ?: return brokenItemStack(this)
            val itemInstance = ItemStack.fromNbt(modernItemTag)
            if (itemInstance.nbt?.contains("Enchantments") == true) {
                itemInstance.enchantments.add(NbtCompound())
            }
            return itemInstance
        } catch (e: Exception) {
            e.printStackTrace()
            return brokenItemStack(this)
        }
    }

    fun NEUItem?.asItemStack(idHint: SkyblockId? = null, loreReplacements: Map<String, String>? = null): ItemStack {
        if (this == null) return brokenItemStack(null, idHint)
        var s = cache[this.skyblockItemId]
        if (s == null) {
            s = asItemStackNow()
            cache[this.skyblockItemId] = s
        }
        if (!loreReplacements.isNullOrEmpty()) {
            s = s.copy()!!
            s.applyLoreReplacements(loreReplacements)
            s.setCustomName(s.name.applyLoreReplacements(loreReplacements))
        }
        return s
    }

    fun ItemStack.applyLoreReplacements(loreReplacements: Map<String, String>) {
        val component = getOrCreateSubNbt("display")
        val lore = component.getOrCreateList("Lore", NbtString.STRING_TYPE)
        val newLore = NbtList()
        lore.forEach {
            newLore.add(
                NbtString.of(
                    Text.Serialization.toJsonString(
                        Text.Serialization.fromJson(it.asString())!!.applyLoreReplacements(loreReplacements)
                    )
                )
            )
        }
        component["Lore"] = newLore
    }

    fun Text.applyLoreReplacements(loreReplacements: Map<String, String>): Text {
        assert(this.siblings.isEmpty())
        var string = this.string
        loreReplacements.forEach { (find, replace) ->
            string = string.replace("{$find}", replace)
        }
        return Text.literal(string).styled { this.style }
    }


    fun NEUItem.getIdentifier() = skyblockId.identifier


    var job: Job? = null

    override fun reload(repository: NEURepository) {
        val j = job
        if (j != null && j.isActive) {
            j.cancel()
        }
        cache.clear()
        isFlawless = true

        job = Firmament.coroutineScope.launch {
            val items = repository.items?.items
            if (items == null) {
                CottonHud.remove(RepoManager.progressBar)
                return@launch
            }
            val recacheItems = I18n.translate("firmament.repo.cache")
            RepoManager.progressBar.reportProgress(recacheItems, 0, items.size)
            CottonHud.add(RepoManager.progressBar)
            var i = 0
            items.values.forEach {
                it.asItemStack() // Rebuild cache
                RepoManager.progressBar.reportProgress(recacheItems, i++, items.size)
            }
            CottonHud.remove(RepoManager.progressBar)
        }
    }

    fun coinItem(coinAmount: Int): ItemStack {
        var uuid = UUID.fromString("2070f6cb-f5db-367a-acd0-64d39a7e5d1b")
        var texture =
            "http://textures.minecraft.net/texture/538071721cc5b4cd406ce431a13f86083a8973e1064d2f8897869930ee6e5237"
        if (coinAmount >= 100000) {
            uuid = UUID.fromString("94fa2455-2881-31fe-bb4e-e3e24d58dbe3")
            texture =
                "http://textures.minecraft.net/texture/c9b77999fed3a2758bfeaf0793e52283817bea64044bf43ef29433f954bb52f6"
        }
        if (coinAmount >= 10000000) {
            uuid = UUID.fromString("0af8df1f-098c-3b72-ac6b-65d65fd0b668")
            texture =
                "http://textures.minecraft.net/texture/7b951fed6a7b2cbc2036916dec7a46c4a56481564d14f945b6ebc03382766d3b"
        }
        val itemStack = ItemStack(Items.PLAYER_HEAD)
        itemStack.setCustomName(Text.literal("§r§6" + NumberFormat.getInstance().format(coinAmount) + " Coins"))
        val nbt: NbtCompound = itemStack.orCreateNbt
        nbt[SkullBlockEntity.SKULL_OWNER_KEY] = NbtHelper.writeGameProfile(
            NbtCompound(),
            GameProfile(uuid, "CoolGuy123").also {
                it.setTextures(
                    MinecraftTexturesPayloadKt(
                        mapOf(
                            MinecraftProfileTexture.Type.SKIN to MinecraftProfileTextureKt(texture),
                        ),
                        uuid,
                        "CoolGuy123"
                    )
                )
            }
        )
        return itemStack
    }
}


operator fun NbtCompound.set(key: String, value: String) {
    putString(key, value)
}

operator fun NbtCompound.set(key: String, value: NbtElement) {
    put(key, value)
}
