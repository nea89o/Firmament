package moe.nea.firmament.features.debug

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Duration.Companion.seconds
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.ProfileComponent
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.Vec3d
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.EntityUpdateEvent
import moe.nea.firmament.events.IsSlotProtectedEvent
import moe.nea.firmament.util.ClipboardUtils
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.TimeMark
import moe.nea.firmament.util.extraAttributes
import moe.nea.firmament.util.json.toJsonArray
import moe.nea.firmament.util.math.GChainReconciliation.shortenCycle
import moe.nea.firmament.util.mc.displayNameAccordingToNbt
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.rawSkyBlockId
import moe.nea.firmament.util.toTicks
import moe.nea.firmament.util.tr


object SkinPreviews {

	// TODO: add pet support
	@Subscribe
	fun onEntityUpdate(event: EntityUpdateEvent) {
		if (!isRecording) return
		if (event.entity.pos != pos)
			return
		val entity = event.entity as? LivingEntity ?: return
		val stack = entity.getEquippedStack(EquipmentSlot.HEAD) ?: return
		val profile = stack.get(DataComponentTypes.PROFILE) ?: return
		if (!profile.isCompleted) {
			lastDiscard = TimeMark.now()
			animation.clear()
			MC.sendChat(
				tr(
					"firmament.dev.skinpreviews.discarding",
					"Encountered unloaded skin, discarding all previews skin frames."
				)
			)
			return
		}
		if (profile == animation.lastOrNull()) return
		animation.add(profile)
		val shortened = animation.shortenCycle()
		if (shortened.size <= (animation.size / 2).coerceAtLeast(1) && lastDiscard.passedTime() > 2.seconds) {
			val tickEstimation = (lastDiscard.passedTime() / animation.size).toTicks()
			val skinName = if (skinColor != null) "${skinId}_${skinColor?.replace(" ", "_")?.uppercase()}" else skinId!!
			val json =
				buildJsonObject {
					put("ticks", tickEstimation)
					put(
						"textures",
						shortened.map {
							it.gameProfile().id.toString() + ":" + it.properties()["textures"].first().value()
						}.toJsonArray()
					)
				}
			MC.sendChat(
				tr(
					"firmament.dev.skinpreviews.done",
					"Observed a total of ${animation.size} elements, which could be shortened to a cycle of ${shortened.size}. Copying JSON array. Estimated ticks per frame: $tickEstimation."
				)
			)
			isRecording = false
			ClipboardUtils.setTextContent(JsonPrimitive(skinName).toString() + ":" + json.toString())
		}
	}

	var animation = mutableListOf<ProfileComponent>()
	var pos = Vec3d(-1.0, 72.0, -101.25)
	var isRecording = false
	var skinColor: String? = null
	var skinId: String? = null
	var lastDiscard = TimeMark.farPast()

	@Subscribe
	fun onActivate(event: IsSlotProtectedEvent) {
		if (!PowerUserTools.TConfig.autoCopyAnimatedSkins) return
		val lastLine = event.itemStack.loreAccordingToNbt.lastOrNull()?.string
		if (lastLine != "Right-click to preview!" && lastLine != "Click to preview!") return
		lastDiscard = TimeMark.now()
		val stackName = event.itemStack.displayNameAccordingToNbt.string
		if (stackName == "FIRE SALE!") {
			skinColor = null
			skinId = event.itemStack.rawSkyBlockId
		} else {
			skinColor = stackName
		}
		animation.clear()
		isRecording = true
		MC.sendChat(tr("firmament.dev.skinpreviews.start", "Starting to observe items"))
	}
}
