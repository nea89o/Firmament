/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.mining

import java.util.regex.Pattern
import org.intellij.lang.annotations.Language
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import moe.nea.firmament.events.HudRenderEvent
import moe.nea.firmament.events.ProcessChatEvent
import moe.nea.firmament.events.SlotClickEvent
import moe.nea.firmament.events.WorldReadyEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.TimeMark
import moe.nea.firmament.util.item.displayNameAccordingToNbt
import moe.nea.firmament.util.item.loreAccordingToNbt
import moe.nea.firmament.util.render.RenderCircleProgress
import moe.nea.firmament.util.unformattedString
import moe.nea.firmament.util.useMatch

object PickaxeAbility : FirmamentFeature {
    override val identifier: String
        get() = "pickaxe-info"


    object TConfig : ManagedConfig(identifier) {
        val cooldownEnabled by toggle("ability-cooldown") { true }
        val cooldownScale by integer("ability-scale", 16, 64) { 16 }
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

    override fun onLoad() {
        HudRenderEvent.subscribe(this::renderHud)
        WorldReadyEvent.subscribe {
            lastUsage.clear()
            lobbyJoinTime = TimeMark.now()
            abilityOverride = null
        }
        ProcessChatEvent.subscribe {
            pattern.useMatch(it.unformattedString) {
                lastUsage[group("name")] = TimeMark.now()
            }
            abilitySwitchPattern.useMatch(it.unformattedString) {
                abilityOverride = group("ability")
            }
        }
        SlotClickEvent.subscribe {
            if (MC.screen?.title?.unformattedString == "Heart of the Mountain") {
                val name = it.stack.displayNameAccordingToNbt?.unformattedString ?: return@subscribe
                val cooldown = it.stack.loreAccordingToNbt.firstNotNullOfOrNull {
                    cooldownPattern.useMatch(it.value?.unformattedString ?: return@firstNotNullOfOrNull null) {
                        parseTimePattern(group("cooldown"))
                    }
                } ?: return@subscribe
                defaultAbilityDurations[name] = cooldown
            }
        }
    }

    val pattern = Pattern.compile("You used your (?<name>.*) Pickaxe Ability!")

    data class PickaxeAbilityData(
        val name: String,
        val cooldown: Duration,
    )

    fun getCooldownFromLore(itemStack: ItemStack): PickaxeAbilityData? {
        val lore = itemStack.loreAccordingToNbt
        if (!lore.any { it.value?.unformattedString?.contains("Breaking Power") == true })
            return null
        val cooldown = lore.firstNotNullOfOrNull {
            cooldownPattern.useMatch(it.value?.unformattedString ?: return@firstNotNullOfOrNull null) {
                parseTimePattern(group("cooldown"))
            }
        } ?: return null
        val name = lore.firstNotNullOfOrNull {
            abilityPattern.useMatch(it.value?.unformattedString ?: return@firstNotNullOfOrNull null) {
                group("name")
            }
        } ?: return null
        return PickaxeAbilityData(name, cooldown)
    }

    @Language("RegExp")
    val TIME_PATTERN = "[0-9]+[ms]"
    val cooldownPattern = Pattern.compile("Cooldown: (?<cooldown>$TIME_PATTERN)")
    val abilityPattern = Pattern.compile("Ability: (?<name>.*) {2}RIGHT CLICK")
    val abilitySwitchPattern =
        Pattern.compile("You selected (?<ability>.*) as your Pickaxe Ability\\. This ability will apply to all of your pickaxes!")

    fun parseTimePattern(text: String): Duration {
        val length = text.dropLast(1).toInt()
        return when (text.last()) {
            'm' -> length.minutes
            's' -> length.seconds
            else -> error("Invalid pattern for time $text")
        }
    }

    private fun renderHud(event: HudRenderEvent) {
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
            event.context, Identifier("firmament", "textures/gui/circle.png"),
            getCooldownPercentage(ability.name, ability.cooldown).toFloat(),
            0f, 1f, 0f, 1f
        )
        event.context.matrices.pop()
    }
}
