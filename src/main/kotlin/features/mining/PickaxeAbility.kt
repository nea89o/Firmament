package moe.nea.firmament.features.mining

import java.util.regex.Pattern
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import net.minecraft.client.MinecraftClient
import net.minecraft.client.toast.SystemToast
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.DyeColor
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.StringIdentifiable
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.HudRenderEvent
import moe.nea.firmament.events.ProcessChatEvent
import moe.nea.firmament.events.ProfileSwitchEvent
import moe.nea.firmament.events.SlotClickEvent
import moe.nea.firmament.events.UseItemEvent
import moe.nea.firmament.events.WorldReadyEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.DurabilityBarEvent
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.SHORT_NUMBER_FORMAT
import moe.nea.firmament.util.SkyBlockIsland
import moe.nea.firmament.util.TIME_PATTERN
import moe.nea.firmament.util.TimeMark
import moe.nea.firmament.util.extraAttributes
import moe.nea.firmament.util.mc.displayNameAccordingToNbt
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.parseShortNumber
import moe.nea.firmament.util.parseTimePattern
import moe.nea.firmament.util.red
import moe.nea.firmament.util.render.RenderCircleProgress
import moe.nea.firmament.util.render.lerp
import moe.nea.firmament.util.skyblock.AbilityUtils
import moe.nea.firmament.util.skyblock.ItemType
import moe.nea.firmament.util.toShedaniel
import moe.nea.firmament.util.tr
import moe.nea.firmament.util.unformattedString
import moe.nea.firmament.util.useMatch

object PickaxeAbility : FirmamentFeature {
	override val identifier: String
		get() = "pickaxe-info"


	object TConfig : ManagedConfig(identifier, Category.MINING) {
		val cooldownEnabled by toggle("ability-cooldown") { false }
		val cooldownScale by integer("ability-scale", 16, 64) { 16 }
		val cooldownReadyToast by toggle("ability-cooldown-toast") { false }
		val drillFuelBar by toggle("fuel-bar") { true }
		val blockOnPrivateIsland by choice(
			"block-on-dynamic",
		) {
			BlockPickaxeAbility.ONLY_DESTRUCTIVE
		}
	}

	enum class BlockPickaxeAbility : StringIdentifiable {
		NEVER,
		ALWAYS,
		ONLY_DESTRUCTIVE;

		override fun asString(): String {
			return name
		}
	}

	var lobbyJoinTime = TimeMark.farPast()
	var lastUsage = mutableMapOf<String, TimeMark>()
	var abilityOverride: String? = null
	var defaultAbilityDurations = mutableMapOf<String, Duration>(
		"Mining Speed Boost" to 120.seconds,
		"Pickobulus" to 110.seconds,
		"Gemstone Infusion" to 140.seconds,
		"Hazardous Miner" to 140.seconds,
		"Maniac Miner" to 59.seconds,
		"Vein Seeker" to 60.seconds
	)
	val destructiveAbilities = setOf("Pickobulus")
	val pickaxeTypes = setOf(ItemType.PICKAXE, ItemType.DRILL, ItemType.GAUNTLET)

	override val config: ManagedConfig
		get() = TConfig

	fun getCooldownPercentage(name: String, cooldown: Duration): Double {
		val sinceLastUsage = lastUsage[name]?.passedTime() ?: Duration.INFINITE
		val sinceLobbyJoin = lobbyJoinTime.passedTime()
		if (SBData.skyblockLocation == SkyBlockIsland.MINESHAFT) {
			if (sinceLobbyJoin < sinceLastUsage) {
				return 1.0
			}
		}
		if (sinceLastUsage < cooldown)
			return sinceLastUsage / cooldown
		return 1.0
	}

	@Subscribe
	fun onPickaxeRightClick(event: UseItemEvent) {
		if (TConfig.blockOnPrivateIsland == BlockPickaxeAbility.NEVER) return
		if (SBData.skyblockLocation != SkyBlockIsland.PRIVATE_ISLAND && SBData.skyblockLocation != SkyBlockIsland.GARDEN) return
		val itemType = ItemType.fromItemStack(event.item)
		if (itemType !in pickaxeTypes) return
		val ability = AbilityUtils.getAbilities(event.item)
		val shouldBlock = when (TConfig.blockOnPrivateIsland) {
			BlockPickaxeAbility.NEVER -> false
			BlockPickaxeAbility.ALWAYS -> ability.any()
			BlockPickaxeAbility.ONLY_DESTRUCTIVE -> ability.any { it.name in destructiveAbilities }
		}
		if (shouldBlock) {
			MC.sendChat(tr("firmament.pickaxe.blocked",
			               "Firmament blocked a pickaxe ability from being used on a private island.")
				            .red() // TODO: .clickCommand("firm confignavigate ${TConfig.identifier} block-on-dynamic")
			)
			event.cancel()
		}
	}

	@Subscribe
	fun onSlotClick(it: SlotClickEvent) {
		if (MC.screen?.title?.unformattedString == "Heart of the Mountain") {
			val name = it.stack.displayNameAccordingToNbt.unformattedString
			val cooldown = it.stack.loreAccordingToNbt.firstNotNullOfOrNull {
				cooldownPattern.useMatch(it.unformattedString) {
					parseTimePattern(group("cooldown"))
				}
			} ?: return
			defaultAbilityDurations[name] = cooldown
		}
	}

	@Subscribe
	fun onDurabilityBar(it: DurabilityBarEvent) {
		if (!TConfig.drillFuelBar) return
		val lore = it.item.loreAccordingToNbt
		if (lore.lastOrNull()?.unformattedString?.contains("DRILL") != true) return
		val maxFuel = lore.firstNotNullOfOrNull {
			fuelPattern.useMatch(it.unformattedString) {
				parseShortNumber(group("maxFuel"))
			}
		} ?: return
		val extra = it.item.extraAttributes
		val fuel = extra.getInt("drill_fuel").getOrNull() ?: return
		var percentage = fuel / maxFuel.toFloat()
		if (percentage > 1f) percentage = 1f
		it.barOverride = DurabilityBarEvent.DurabilityBar(
			lerp(
				DyeColor.RED.toShedaniel(),
				DyeColor.GREEN.toShedaniel(),
				percentage
			), percentage
		)
	}

	@Subscribe
	fun onChatMessage(it: ProcessChatEvent) {
		abilityUsePattern.useMatch(it.unformattedString) {
			lastUsage[group("name")] = TimeMark.now()
			abilityOverride = group("name")
		}
		abilitySwitchPattern.useMatch(it.unformattedString) {
			abilityOverride = group("ability")
		}
		pickaxeAbilityCooldownPattern.useMatch(it.unformattedString) {
			val ability = abilityOverride ?: return@useMatch
			val remainingCooldown = parseTimePattern(group("remainingCooldown"))
			val length = defaultAbilityDurations[ability] ?: return@useMatch
			lastUsage[ability] = TimeMark.ago(length - remainingCooldown)
		}
		nowAvailable.useMatch(it.unformattedString) {
			val ability = group("name")
			lastUsage[ability] = TimeMark.farPast()
			if (!TConfig.cooldownReadyToast) return
			val mc: MinecraftClient = MinecraftClient.getInstance()
			mc.toastManager.add(
				SystemToast.create(mc, SystemToast.Type.NARRATOR_TOGGLE, tr("firmament.pickaxe.ability-ready","Pickaxe Cooldown"), tr("firmament.pickaxe.ability-ready.desc", "Pickaxe ability is ready!"))
			)
		}
	}

	@Subscribe
	fun onWorldReady(event: WorldReadyEvent) {
		lobbyJoinTime = TimeMark.now()
		abilityOverride = null
	}

	@Subscribe
	fun onProfileSwitch(event: ProfileSwitchEvent) {
		lastUsage.entries.removeIf {
			it.value < lobbyJoinTime
		}
	}

	val abilityUsePattern = Pattern.compile("You used your (?<name>.*) Pickaxe Ability!")
	val fuelPattern = Pattern.compile("Fuel: .*/(?<maxFuel>$SHORT_NUMBER_FORMAT)")
	val pickaxeAbilityCooldownPattern =
		Pattern.compile("Your pickaxe ability is on cooldown for (?<remainingCooldown>$TIME_PATTERN)\\.")
	val nowAvailable = Pattern.compile("(?<name>[a-zA-Z0-9 ]+) is now available!")

	data class PickaxeAbilityData(
		val name: String,
		val cooldown: Duration,
	)

	fun getCooldownFromLore(itemStack: ItemStack): PickaxeAbilityData? {
		val lore = itemStack.loreAccordingToNbt
		if (!lore.any { it.unformattedString.contains("Breaking Power") })
			return null
		val ability = AbilityUtils.getAbilities(itemStack).firstOrNull() ?: return null
		return PickaxeAbilityData(ability.name, ability.cooldown ?: return null)
	}

	val cooldownPattern = Pattern.compile("Cooldown: (?<cooldown>$TIME_PATTERN)")
	val abilitySwitchPattern =
		Pattern.compile("You selected (?<ability>.*) as your Pickaxe Ability\\. This ability will apply to all of your pickaxes!")

	@Subscribe
	fun renderHud(event: HudRenderEvent) {
		if (!TConfig.cooldownEnabled) return
		if (!event.isRenderingCursor) return
		var ability = getCooldownFromLore(MC.player?.getStackInHand(Hand.MAIN_HAND) ?: return) ?: return
		defaultAbilityDurations[ability.name] = ability.cooldown
		val ao = abilityOverride
		if (ao != ability.name && ao != null) {
			ability = PickaxeAbilityData(ao, defaultAbilityDurations[ao] ?: 120.seconds)
		}
		event.context.matrices.push()
		event.context.matrices.translate(MC.window.scaledWidth / 2F, MC.window.scaledHeight / 2F, 0F)
		event.context.matrices.scale(TConfig.cooldownScale.toFloat(), TConfig.cooldownScale.toFloat(), 1F)
		RenderCircleProgress.renderCircle(
			event.context, Identifier.of("firmament", "textures/gui/circle.png"),
			getCooldownPercentage(ability.name, ability.cooldown).toFloat(),
			0f, 1f, 0f, 1f
		)
		event.context.matrices.pop()
	}
}
