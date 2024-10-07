
package moe.nea.firmament.features.mining

import java.util.regex.Pattern
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import net.minecraft.item.ItemStack
import net.minecraft.util.DyeColor
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.HudRenderEvent
import moe.nea.firmament.events.ProcessChatEvent
import moe.nea.firmament.events.SlotClickEvent
import moe.nea.firmament.events.WorldReadyEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.DurabilityBarEvent
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SHORT_NUMBER_FORMAT
import moe.nea.firmament.util.TIME_PATTERN
import moe.nea.firmament.util.TimeMark
import moe.nea.firmament.util.extraAttributes
import moe.nea.firmament.util.mc.displayNameAccordingToNbt
import moe.nea.firmament.util.mc.loreAccordingToNbt
import moe.nea.firmament.util.parseShortNumber
import moe.nea.firmament.util.parseTimePattern
import moe.nea.firmament.util.render.RenderCircleProgress
import moe.nea.firmament.util.render.lerp
import moe.nea.firmament.util.toShedaniel
import moe.nea.firmament.util.unformattedString
import moe.nea.firmament.util.useMatch

object PickaxeAbility : FirmamentFeature {
    override val identifier: String
        get() = "pickaxe-info"


    object TConfig : ManagedConfig(identifier) {
        val cooldownEnabled by toggle("ability-cooldown") { true }
        val cooldownScale by integer("ability-scale", 16, 64) { 16 }
        val drillFuelBar by toggle("fuel-bar") { true }
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

    override val config: ManagedConfig
        get() = TConfig

    fun getCooldownPercentage(name: String, cooldown: Duration): Double {
        val sinceLastUsage = lastUsage[name]?.passedTime() ?: Duration.INFINITE
        if (sinceLastUsage < cooldown)
            return sinceLastUsage / cooldown
        val sinceLobbyJoin = lobbyJoinTime.passedTime()
        val halfCooldown = cooldown / 2
        if (sinceLobbyJoin < halfCooldown) {
            return (sinceLobbyJoin / halfCooldown)
        }
        return 1.0
    }

    @Subscribe
    fun onSlotClick(it: SlotClickEvent) {
        if (MC.screen?.title?.unformattedString == "Heart of the Mountain") {
            val name = it.stack.displayNameAccordingToNbt?.unformattedString ?: return
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
        if (!extra.contains("drill_fuel")) return
        val fuel = extra.getInt("drill_fuel")
        val percentage = fuel / maxFuel.toFloat()
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
        }
        abilitySwitchPattern.useMatch(it.unformattedString) {
            abilityOverride = group("ability")
        }
    }

    @Subscribe
    fun onWorldReady(event: WorldReadyEvent) {
        lastUsage.clear()
        lobbyJoinTime = TimeMark.now()
        abilityOverride = null
    }

    val abilityUsePattern = Pattern.compile("You used your (?<name>.*) Pickaxe Ability!")
    val fuelPattern = Pattern.compile("Fuel: .*/(?<maxFuel>$SHORT_NUMBER_FORMAT)")

    data class PickaxeAbilityData(
        val name: String,
        val cooldown: Duration,
    )

    fun getCooldownFromLore(itemStack: ItemStack): PickaxeAbilityData? {
        val lore = itemStack.loreAccordingToNbt
        if (!lore.any { it.unformattedString.contains("Breaking Power") == true })
            return null
        val cooldown = lore.firstNotNullOfOrNull {
            cooldownPattern.useMatch(it.unformattedString) {
                parseTimePattern(group("cooldown"))
            }
        } ?: return null
        val name = lore.firstNotNullOfOrNull {
            abilityPattern.useMatch(it.unformattedString) {
                group("name")
            }
        } ?: return null
        return PickaxeAbilityData(name, cooldown)
    }


    val cooldownPattern = Pattern.compile("Cooldown: (?<cooldown>$TIME_PATTERN)")
    val abilityPattern = Pattern.compile("(⦾ )?Ability: (?<name>.*) {2}RIGHT CLICK")
    val abilitySwitchPattern =
        Pattern.compile("You selected (?<ability>.*) as your Pickaxe Ability\\. This ability will apply to all of your pickaxes!")


    @Subscribe
    fun renderHud(event: HudRenderEvent) {
        if (!TConfig.cooldownEnabled) return
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
