package moe.nea.firmament.repo

import io.github.moulberry.repo.IReloadable
import io.github.moulberry.repo.NEURepository
import io.github.moulberry.repo.data.NEUItem
import java.util.Collections
import java.util.NavigableMap
import java.util.TreeMap
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.serializer
import kotlin.jvm.optionals.getOrNull
import kotlin.streams.asSequence
import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text
import moe.nea.firmament.repo.ReforgeStore.kJson
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.SkyBlockIsland
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.mc.FirmamentDataComponentTypes
import moe.nea.firmament.util.mc.displayNameAccordingToNbt
import moe.nea.firmament.util.skyblockId

class MiningRepoData : IReloadable {
	var customMiningAreas: Map<SkyBlockIsland, CustomMiningArea> = mapOf()
		private set
	var customMiningBlocks: List<CustomMiningBlock> = listOf()
		private set
	var toolsByBreakingPower: NavigableMap<BreakingPowerKey, SBItemStack> = Collections.emptyNavigableMap()
		private set


	data class BreakingPowerKey(
		val breakingPower: Int,
		val itemId: SkyblockId? = null
	) {
		companion object {
			val COMPARATOR: Comparator<BreakingPowerKey> =
				Comparator
					.comparingInt<BreakingPowerKey> { it.breakingPower }
					.thenComparing(Comparator.comparing(
						{ it.itemId },
						nullsFirst(Comparator.comparing<SkyblockId, Boolean> { "PICK" in it.neuItem || "BING" in it.neuItem }.thenComparing(Comparator.naturalOrder<SkyblockId>()))))
		}
	}

	override fun reload(repo: NEURepository) {
		customMiningAreas = repo.file("mining/custom_mining_areas.json")
			?.kJson(serializer()) ?: mapOf()
		customMiningBlocks = repo.tree("mining/blocks")
			.asSequence()
			.filter { it.path.endsWith(".json") }
			.map { it.kJson(serializer<CustomMiningBlock>()) }
			.toList()
		toolsByBreakingPower = Collections.unmodifiableNavigableMap(
			repo.items.items
				.values
				.asSequence()
				.map { SBItemStack(it.skyblockId) }
				.filter { it.breakingPower > 0 }
				.associateTo(TreeMap<BreakingPowerKey, SBItemStack>(BreakingPowerKey.COMPARATOR)) {
					BreakingPowerKey(it.breakingPower, it.skyblockId) to it
				})
	}

	fun getToolsThatCanBreak(breakingPower: Int): Collection<SBItemStack> {
		return toolsByBreakingPower.tailMap(BreakingPowerKey(breakingPower, null), true).values
	}

	@Serializable
	data class CustomMiningBlock(
		val breakingPower: Int = 0,
		val blockStrength: Int = 0,
		val name: String? = null,
		val baseDrop: SkyblockId? = null,
		val blocks189: List<Block189> = emptyList()
	) {
		@Transient
		val dropItem = baseDrop?.let(::SBItemStack)
		private val labeledStack by lazy {
			dropItem?.asCopiedItemStack()?.also(::markItemStack)
		}

		private fun markItemStack(itemStack: ItemStack) {
			itemStack.set(FirmamentDataComponentTypes.CUSTOM_MINING_BLOCK_DATA, this)
			if (name != null)
				itemStack.displayNameAccordingToNbt = Text.literal(name)
		}

		fun getDisplayItem(block: Block): ItemStack {
			return labeledStack ?: ItemStack(block).also(::markItemStack)
		}
	}

	@Serializable
	data class Block189(
		val itemId: String,
		val damage: Short = 0,
		val onlyIn: List<SkyBlockIsland>? = null,
	) {
		@Transient
		val block = convertToModernBlock()

		val isCurrentlyActive: Boolean
			get() = isActiveIn(SBData.skyblockLocation ?: SkyBlockIsland.NIL)

		fun isActiveIn(location: SkyBlockIsland) = onlyIn == null || location in onlyIn

		private fun convertToModernBlock(): Block? {
			// TODO: this should be in a shared util, really
			val newCompound = ItemCache.convert189ToModern(NbtCompound().apply {
				putString("id", itemId)
				putShort("Damage", damage)
			}) ?: return null
			val itemStack = ItemStack.fromNbt(MC.defaultRegistries, newCompound).getOrNull() ?: return null
			val blockItem = itemStack.item as? BlockItem ?: return null
			return blockItem.block
		}
	}

	@Serializable
	data class CustomMiningArea(
		val isSpecialMining: Boolean = true
	)


}
