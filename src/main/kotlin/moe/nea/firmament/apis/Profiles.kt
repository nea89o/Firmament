@file:UseSerializers(DashlessUUIDSerializer::class, InstantAsLongSerializer::class)

package moe.nea.firmament.apis

import io.github.moulberry.repo.constants.Leveling
import io.github.moulberry.repo.data.Rarity
import java.util.UUID
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlin.reflect.KProperty1
import net.minecraft.util.DyeColor
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.LegacyFormattingCode
import moe.nea.firmament.util.json.DashlessUUIDSerializer
import moe.nea.firmament.util.json.InstantAsLongSerializer


@Serializable
data class Profiles(
    val success: Boolean,
    val profiles: List<Profile>
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

enum class Skill(val accessor: KProperty1<Member, Double>, val color: DyeColor) {
    FARMING(Member::experienceSkillFarming, DyeColor.YELLOW),
    FORAGING(Member::experienceSkillForaging, DyeColor.BROWN),
    MINING(Member::experienceSkillMining, DyeColor.LIGHT_GRAY),
    ALCHEMY(Member::experienceSkillAlchemy, DyeColor.PURPLE),
    TAMING(Member::experienceSkillTaming, DyeColor.GREEN),
    FISHING(Member::experienceSkillFishing, DyeColor.BLUE),
    RUNECRAFTING(Member::experienceSkillRunecrafting, DyeColor.PINK),
    CARPENTRY(Member::experienceSkillCarpentry, DyeColor.ORANGE),
    COMBAT(Member::experienceSkillCombat, DyeColor.RED),
    SOCIAL(Member::experienceSkillSocial, DyeColor.WHITE),
    ENCHANTING(Member::experienceSkillEnchanting, DyeColor.MAGENTA),
    ;

    fun getMaximumLevel(leveling: Leveling) = leveling.maximumLevels[name.lowercase()] ?: TODO("Repo error")

    fun getLadder(leveling: Leveling): List<Int> {
        if (this == SOCIAL) return leveling.socialExperienceRequiredPerLevel
        if (this == RUNECRAFTING) return leveling.runecraftingExperienceRequiredPerLevel
        return leveling.skillExperienceRequiredPerLevel
    }
}

@Serializable
data class Member(
    val pets: List<Pet>,
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
)

@Serializable
data class PlayerResponse(
    val success: Boolean,
    val player: PlayerData,
)

@Serializable
data class PlayerData(
    val uuid: UUID,
    val firstLogin: Instant,
    val lastLogin: Instant,
    @SerialName("playername")
    val playerName: String,
    val achievementsOneTime: List<String> = listOf(),
    @SerialName("newPackageRank")
    val packageRank: String?,
    val monthlyPackageRank: String? = null,
    val rankPlusColor: String = "GOLD"
) {
    val rankPlusDyeColor = LegacyFormattingCode.values().find { it.name == rankPlusColor } ?: LegacyFormattingCode.GOLD
    val rankData get() = RepoManager.neuRepo.constants.misc.ranks[monthlyPackageRank ?: packageRank]
}

@Serializable
data class AshconNameLookup(
    val username: String,
    val uuid: UUID,
)
