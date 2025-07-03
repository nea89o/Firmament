package moe.nea.firmament.features.inventory

import moe.nea.jarvis.api.Point
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.HudRenderEvent
import moe.nea.firmament.events.SlotRenderEvents
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.FirmFormatters.formatPercent
import moe.nea.firmament.util.FirmFormatters.shortFormat
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.petData
import moe.nea.firmament.util.render.drawGuiTexture
import moe.nea.firmament.util.skyblock.Rarity
import moe.nea.firmament.util.titleCase
import moe.nea.firmament.util.useMatch
import moe.nea.firmament.util.withColor

object PetFeatures : FirmamentFeature {
	override val identifier: String
		get() = "pets"

	override val config: ManagedConfig?
		get() = TConfig

	object TConfig : ManagedConfig(identifier, Category.INVENTORY) {
		val highlightEquippedPet by toggle("highlight-pet") { true }
		var petOverlay by toggle("pet-overlay") { false }
		val petOverlayHud by position("pet-overlay-hud", 80, 10) { Point(0.5, 1.0) }
	}

	val petMenuTitle = "Pets(?: \\([0-9]+/[0-9]+\\))?".toPattern()
	var petItemStack: ItemStack? = null

	@Subscribe
	fun onSlotRender(event: SlotRenderEvents.Before) {
		if (!TConfig.highlightEquippedPet) return
		val stack = event.slot.stack
		if (stack.petData?.active == true)
			petMenuTitle.useMatch(MC.screenName ?: return) {
			petItemStack = stack
			event.context.drawGuiTexture(
				Firmament.identifier("selected_pet_background"),
				event.slot.x, event.slot.y, 16, 16,
			)
		}
	}

	@Subscribe
	fun onRenderHud(it: HudRenderEvent) {
		if (!TConfig.petOverlay || !SBData.isOnSkyblock) return
		val itemStack = petItemStack ?: return
		val petData = petItemStack?.petData ?: return
		val rarity = Rarity.fromNeuRepo(petData.tier)
		val rarityCode = Rarity.colourMap[rarity] ?: Formatting.WHITE
		val xp = petData.level
		val petType = titleCase(petData.type)
		val heldItem = petData.heldItem?.let { item -> "Held Item: ${titleCase(item)}" }

		it.context.matrices.push()
		TConfig.petOverlayHud.applyTransformations(it.context.matrices)

		val lines = mutableListOf<Text>()
		it.context.matrices.push()
		it.context.matrices.translate(-0.5, -0.5, 0.0)
		it.context.matrices.scale(2f, 2f, 1f)
		it.context.drawItem(itemStack, 0, 0)
		it.context.matrices.pop()

		lines.add(Text.literal("[Lvl ${xp.currentLevel}] ").append(Text.literal(petType).withColor(rarityCode)))
		if (heldItem != null) lines.add(Text.literal(heldItem))
		if (xp.currentLevel != xp.maxLevel) lines.add(Text.literal("Required L${xp.currentLevel + 1}: ${shortFormat(xp.expInCurrentLevel.toDouble())}/${shortFormat(xp.expRequiredForNextLevel.toDouble())} (${formatPercent(xp.percentageToNextLevel.toDouble())})"))
		lines.add(Text.literal("Required L100: ${shortFormat(xp.expTotal.toDouble())}/${shortFormat(xp.expRequiredForMaxLevel.toDouble())} (${formatPercent(xp.percentageToMaxLevel.toDouble())})"))

		for ((index, line) in lines.withIndex()) {
			it.context.drawText(MC.font, line.copy().withColor(Formatting.GRAY), 36, MC.font.fontHeight * index, -1, true)
		}

		it.context.matrices.pop()
	}
}
