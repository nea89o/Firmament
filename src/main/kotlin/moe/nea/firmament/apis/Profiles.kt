/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

@file:UseSerializers(DashlessUUIDSerializer::class, InstantAsLongSerializer::class)

package moe.nea.firmament.apis

import io.github.moulberry.repo.constants.Leveling
import io.github.moulberry.repo.data.Rarity
import java.util.*
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.reflect.KProperty1
import net.minecraft.util.DyeColor
import net.minecraft.util.Formatting
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.LegacyFormattingCode
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.json.DashlessUUIDSerializer
import moe.nea.firmament.util.json.InstantAsLongSerializer


@Serializable
data class CollectionSkillData(
    val items: Map<CollectionType, CollectionInfo>
)

@Serializable
data class CollectionResponse(
    val success: Boolean,
    val collections: Map<String, CollectionSkillData>
)

@Serializable
data class CollectionInfo(
    val name: String,
    val maxTiers: Int,
    val tiers: List<CollectionTier>
)

@Serializable
data class CollectionTier(
    val tier: Int,
    val amountRequired: Long,
    val unlocks: List<String>,
)


@Serializable
data class Profiles(
    val success: Boolean,
    val profiles: List<Profile>?
)

@Serializable
data class Profile(
    @SerialName("profile_id")
    val profileId: UUID,
    @SerialName("cute_name")
    val cuteName: String,
    val selected: Boolean = false,
    val members: Map<UUID, Member>,
)

enum class Skill(val accessor: KProperty1<Member, Double>, val color: DyeColor, val icon: SkyblockId) {
    FARMING(Member::experienceSkillFarming, DyeColor.YELLOW, SkyblockId("ROOKIE_HOE")),
    FORAGING(Member::experienceSkillForaging, DyeColor.BROWN, SkyblockId("TREECAPITATOR_AXE")),
    MINING(Member::experienceSkillMining, DyeColor.LIGHT_GRAY, SkyblockId("DIAMOND_PICKAXE")),
    ALCHEMY(Member::experienceSkillAlchemy, DyeColor.PURPLE, SkyblockId("BREWING_STAND")),
    TAMING(Member::experienceSkillTaming, DyeColor.GREEN, SkyblockId("SUPER_EGG")),
    FISHING(Member::experienceSkillFishing, DyeColor.BLUE, SkyblockId("FARMER_ROD")),
    RUNECRAFTING(Member::experienceSkillRunecrafting, DyeColor.PINK, SkyblockId("MUSIC_RUNE;1")),
    CARPENTRY(Member::experienceSkillCarpentry, DyeColor.ORANGE, SkyblockId("WORKBENCH")),
    COMBAT(Member::experienceSkillCombat, DyeColor.RED, SkyblockId("UNDEAD_SWORD")),
    SOCIAL(Member::experienceSkillSocial, DyeColor.WHITE, SkyblockId("EGG_HUNT")),
    ENCHANTING(Member::experienceSkillEnchanting, DyeColor.MAGENTA, SkyblockId("ENCHANTMENT_TABLE")),
    ;

    fun getMaximumLevel(leveling: Leveling) = leveling.maximumLevels[name.lowercase()] ?: TODO("Repo error")

    fun getLadder(leveling: Leveling): List<Int> {
        if (this == SOCIAL) return leveling.socialExperienceRequiredPerLevel
        if (this == RUNECRAFTING) return leveling.runecraftingExperienceRequiredPerLevel
        return leveling.skillExperienceRequiredPerLevel
    }
}

enum class CollectionCategory(val skill: Skill?, val color: DyeColor, val icon: SkyblockId) {
    FARMING(Skill.FARMING, DyeColor.YELLOW, SkyblockId("ROOKIE_HOE")),
    FORAGING(Skill.FORAGING, DyeColor.BROWN, SkyblockId("TREECAPITATOR_AXE")),
    MINING(Skill.MINING, DyeColor.LIGHT_GRAY, SkyblockId("DIAMOND_PICKAXE")),
    FISHING(Skill.FISHING, DyeColor.BLUE, SkyblockId("FARMER_ROD")),
    COMBAT(Skill.COMBAT, DyeColor.RED, SkyblockId("UNDEAD_SWORD")),
    RIFT(null, DyeColor.PURPLE, SkyblockId("SKYBLOCK_MOTE")),
}

@Serializable
@JvmInline
value class CollectionType(val string: String) {
    val skyblockId get() = SkyblockId(string.replace(":", "-").replace("MUSHROOM_COLLECTION", "HUGE_MUSHROOM_2"))
}

@Serializable
data class Member(
    val pets: List<Pet> = listOf(),
    @SerialName("coop_invitation")
    val coopInvitation: CoopInvitation? = null,
    @SerialName("experience_skill_farming")
    val experienceSkillFarming: Double = 0.0,
    @SerialName("experience_skill_alchemy")
    val experienceSkillAlchemy: Double = 0.0,
    @SerialName("experience_skill_combat")
    val experienceSkillCombat: Double = 0.0,
    @SerialName("experience_skill_taming")
    val experienceSkillTaming: Double = 0.0,
    @SerialName("experience_skill_social2")
    val experienceSkillSocial: Double = 0.0,
    @SerialName("experience_skill_enchanting")
    val experienceSkillEnchanting: Double = 0.0,
    @SerialName("experience_skill_fishing")
    val experienceSkillFishing: Double = 0.0,
    @SerialName("experience_skill_foraging")
    val experienceSkillForaging: Double = 0.0,
    @SerialName("experience_skill_mining")
    val experienceSkillMining: Double = 0.0,
    @SerialName("experience_skill_runecrafting")
    val experienceSkillRunecrafting: Double = 0.0,
    @SerialName("experience_skill_carpentry")
    val experienceSkillCarpentry: Double = 0.0,
    val collection: Map<CollectionType, Long> = mapOf()
)

@Serializable
data class CoopInvitation(
    val timestamp: Instant,
    @SerialName("invited_by")
    val invitedBy: UUID? = null,
    val confirmed: Boolean,
)

@JvmInline
@Serializable
value class PetType(val name: String)

@Serializable
data class Pet(
    val uuid: UUID?,
    val type: PetType,
    val exp: Double,
    val active: Boolean,
    val tier: Rarity,
    val candyUsed: Int,
    val heldItem: String?,
    val skin: String?,
) {
    val itemId get() = SkyblockId("${type.name};${tier.ordinal}")
}

@Serializable
data class PlayerResponse(
    val success: Boolean,
    val player: PlayerData,
)

@Serializable
data class PlayerData(
    val uuid: UUID,
    val firstLogin: Instant,
    val lastLogin: Instant? = null,
    @SerialName("playername")
    val playerName: String,
    val achievementsOneTime: List<String> = listOf(),
    @SerialName("newPackageRank")
    val packageRank: String? = null,
    val monthlyPackageRank: String? = null,
    val rankPlusColor: String = "GOLD"
) {
    val rankPlusDyeColor = LegacyFormattingCode.values().find { it.name == rankPlusColor } ?: LegacyFormattingCode.GOLD
    val rankData get() = RepoManager.neuRepo.constants.misc.ranks[if (monthlyPackageRank == "NONE" || monthlyPackageRank == null) packageRank else monthlyPackageRank]
    fun getDisplayName(name: String = playerName) = rankData?.let {
        ("§${it.color}[${it.tag}${rankPlusDyeColor.modern}" +
            "${it.plus ?: ""}§${it.color}] $name")
    } ?: "${Formatting.GRAY}$name"


}

@Serializable
data class AshconNameLookup(
    val username: String,
    val uuid: UUID,
)
