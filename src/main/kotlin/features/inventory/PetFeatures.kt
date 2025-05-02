package moe.nea.firmament.features.inventory

import moe.nea.jarvis.api.Point
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.HudRenderEvent
import moe.nea.firmament.events.SlotRenderEvents
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.HypixelPetInfo
import moe.nea.firmament.util.LegacyFormattingCode
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.petData
import moe.nea.firmament.util.render.drawGuiTexture
import moe.nea.firmament.util.skyblock.Rarity
import moe.nea.firmament.util.titleCase
import moe.nea.firmament.util.useMatch

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
	var activePetData: HypixelPetInfo? = null
	var petItemStack: ItemStack? = null

	@Subscribe
	fun onSlotRender(event: SlotRenderEvents.Before) {
		if (!TConfig.highlightEquippedPet) return
		val stack = event.slot.stack
		if (stack.petData?.active == true) {
			activePetData = stack.petData
			petItemStack = stack
			petMenuTitle.useMatch(MC.screenName ?: return) {
				event.context.drawGuiTexture(
					event.slot.x, event.slot.y, 0, 16, 16,
					Identifier.of("firmament:selected_pet_background")
				)
			}
		}
	}

	@Subscribe
	fun onRenderHud(it: HudRenderEvent) {
		if (!TConfig.petOverlay) return
		val itemStack = petItemStack ?: return
		val petData = activePetData ?: return
		val rarity = Rarity.fromString(petData.tier.name)
		val rarityCode = Rarity.colourMap[rarity]?.code ?: "f"
		val xp = petData.level
		val petType = titleCase(petData.type)
		val heldItem = petData.heldItem?.let { item -> "Held Item: ${titleCase(item)}" }

		it.context.matrices.push()
		TConfig.petOverlayHud.applyTransformations(it.context.matrices)

		val lines: MutableList<String> = ArrayList<String>()
		it.context.matrices.push()
		it.context.matrices.translate(-0.5, -0.5, 0.0)
		it.context.matrices.scale(2f, 2f, 1f)
		it.context.drawItem(itemStack, 0, 0)
		it.context.matrices.pop()


		lines.add("[Lvl ${xp.currentLevel}] §$rarityCode$petType")
		if (heldItem != null) lines.add(heldItem)
		if (xp.currentLevel != xp.maxLevel) lines.add("Required L${xp.currentLevel + 1}: ${FirmFormatters.shortFormat(xp.expInCurrentLevel.toDouble())}/${FirmFormatters.shortFormat(xp.expRequiredForNextLevel.toDouble())} (${xp.percentageToNextLevel * 100}%)")
		lines.add("Required L100: ${FirmFormatters.shortFormat(xp.expTotal.toDouble())}/${FirmFormatters.shortFormat(xp.expRequiredForMaxLevel.toDouble())} (${xp.percentageToMaxLevel * 100}%)")

		for ((index, line) in lines.withIndex()) {
			it.context.drawText(MC.font, "${LegacyFormattingCode.GRAY.formattingCode}$line", 36, MC.font.fontHeight * index, -1, false)
		}

		it.context.matrices.pop()
	}
}
