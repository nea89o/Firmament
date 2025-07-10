package moe.nea.firmament.features.fixes

import moe.nea.jarvis.api.Point
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.text.Text
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.HudRenderEvent
import moe.nea.firmament.events.WorldKeyboardEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.tr

object Fixes : FirmamentFeature {
	override val identifier: String
		get() = "fixes"

	object TConfig : ManagedConfig(identifier, Category.MISC) { // TODO: split this config
		val fixUnsignedPlayerSkins by toggle("player-skins") { true }
		var autoSprint by toggle("auto-sprint") { false }
		val autoSprintKeyBinding by keyBindingWithDefaultUnbound("auto-sprint-keybinding")
		val autoSprintUnderWater by toggle("auto-sprint-underwater") { true }
		val autoSprintHud by position("auto-sprint-hud", 80, 10) { Point(0.0, 1.0) }
		val peekChat by keyBindingWithDefaultUnbound("peek-chat")
		val hidePotionEffects by toggle("hide-mob-effects") { false }
		val hidePotionEffectsHud by toggle("hide-potion-effects-hud") { false }
		val noHurtCam by toggle("disable-hurt-cam") { false }
		val hideSlotHighlights by toggle("hide-slot-highlights") { false }
		val hideRecipeBook by toggle("hide-recipe-book") { false }
		val hideOffHand by toggle("hide-off-hand") { false }
	}

	override val config: ManagedConfig
		get() = TConfig

	fun handleIsPressed(
		keyBinding: KeyBinding,
		cir: CallbackInfoReturnable<Boolean>
	) {
		if (keyBinding !== MinecraftClient.getInstance().options.sprintKey) return
		if (!TConfig.autoSprint) return
		val player = MC.player ?: return
		if (player.isSprinting) return
		if (!TConfig.autoSprintUnderWater && player.isTouchingWater) return
		cir.returnValue = true
	}

	@Subscribe
	fun onRenderHud(it: HudRenderEvent) {
		if (!TConfig.autoSprintKeyBinding.isBound) return
		it.context.matrices.push()
		TConfig.autoSprintHud.applyTransformations(it.context.matrices)
		it.context.drawText(
			MC.font, (
				if (MC.player?.isSprinting == true) {
					Text.translatable("firmament.fixes.auto-sprint.sprinting")
				} else if (TConfig.autoSprint) {
					if (!TConfig.autoSprintUnderWater && MC.player?.isTouchingWater == true)
						tr("firmament.fixes.auto-sprint.under-water", "In Water")
					else
						Text.translatable("firmament.fixes.auto-sprint.on")
				} else {
					Text.translatable("firmament.fixes.auto-sprint.not-sprinting")
				}
				), 0, 0, -1, true
		)
		it.context.matrices.pop()
	}

	@Subscribe
	fun onWorldKeyboard(it: WorldKeyboardEvent) {
		if (it.matches(TConfig.autoSprintKeyBinding)) {
			TConfig.autoSprint = !TConfig.autoSprint
		}
	}

	fun shouldPeekChat(): Boolean {
		return TConfig.peekChat.isPressed(atLeast = true)
	}
}
