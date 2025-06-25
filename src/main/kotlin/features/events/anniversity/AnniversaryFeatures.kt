
package moe.nea.firmament.features.events.anniversity

import io.github.notenoughupdates.moulconfig.observer.ObservableList
import io.github.notenoughupdates.moulconfig.xml.Bind
import moe.nea.jarvis.api.Point
import kotlin.time.Duration.Companion.seconds
import net.minecraft.entity.passive.PigEntity
import net.minecraft.util.math.BlockPos
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.EntityInteractionEvent
import moe.nea.firmament.events.ProcessChatEvent
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.events.WorldReadyEvent
import moe.nea.firmament.features.FirmamentFeature
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.gui.hud.MoulConfigHud
import moe.nea.firmament.repo.ExpensiveItemCacheApi
import moe.nea.firmament.repo.ItemNameLookup
import moe.nea.firmament.repo.SBItemStack
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SHORT_NUMBER_FORMAT
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.TimeMark
import moe.nea.firmament.util.parseShortNumber
import moe.nea.firmament.util.useMatch

object AnniversaryFeatures : FirmamentFeature {
    override val identifier: String
        get() = "anniversary"

    object TConfig : ManagedConfig(identifier, Category.EVENTS) {
        val enableShinyPigTracker by toggle("shiny-pigs") {true}
        val trackPigCooldown by position("pig-hud", 200, 300) { Point(0.1, 0.2) }
    }

    override val config: ManagedConfig?
        get() = TConfig

    data class ClickedPig(
        val clickedAt: TimeMark,
        val startLocation: BlockPos,
        val pigEntity: PigEntity
    ) {
        @Bind("timeLeft")
        fun getTimeLeft(): Double = 1 - clickedAt.passedTime() / pigDuration
    }

    val clickedPigs = ObservableList<ClickedPig>(mutableListOf())
    var lastClickedPig: PigEntity? = null

    val pigDuration = 90.seconds

    @Subscribe
    fun onTick(event: TickEvent) {
        clickedPigs.removeIf { it.clickedAt.passedTime() > pigDuration }
    }

    val pattern = "SHINY! You extracted (?<reward>.*) from the piglet's orb!".toPattern()

    @Subscribe
    fun onChat(event: ProcessChatEvent) {
        if(!TConfig.enableShinyPigTracker)return
        if (event.unformattedString == "Oink! Bring the pig back to the Shiny Orb!") {
            val pig = lastClickedPig ?: return
            // TODO: store proper location based on the orb location, maybe
            val startLocation = pig.blockPos ?: return
            clickedPigs.add(ClickedPig(TimeMark.now(), startLocation, pig))
            lastClickedPig = null
        }
        if (event.unformattedString == "SHINY! The orb is charged! Click on it for loot!") {
            val player = MC.player ?: return
            val lowest =
                clickedPigs.minByOrNull { it.startLocation.getSquaredDistance(player.pos) } ?: return
            clickedPigs.remove(lowest)
        }
        pattern.useMatch(event.unformattedString) {
            val reward = group("reward")
            val parsedReward = parseReward(reward)
            addReward(parsedReward)
            PigCooldown.rewards.atOnce {
                PigCooldown.rewards.clear()
                rewards.mapTo(PigCooldown.rewards) { PigCooldown.DisplayReward(it) }
            }
        }
    }

    fun addReward(reward: Reward) {
        val it = rewards.listIterator()
        while (it.hasNext()) {
            val merged = reward.mergeWith(it.next()) ?: continue
            it.set(merged)
            return
        }
        rewards.add(reward)
    }

    val rewards = mutableListOf<Reward>()

    fun <T> ObservableList<T>.atOnce(block: () -> Unit) {
        val oldObserver = observer
        observer = null
        block()
        observer = oldObserver
        update()
    }

    sealed interface Reward {
        fun mergeWith(other: Reward): Reward?
        data class EXP(val amount: Double, val skill: String) : Reward {
            override fun mergeWith(other: Reward): Reward? {
                if (other is EXP && other.skill == skill)
                    return EXP(amount + other.amount, skill)
                return null
            }
        }

        data class Coins(val amount: Double) : Reward {
            override fun mergeWith(other: Reward): Reward? {
                if (other is Coins)
                    return Coins(other.amount + amount)
                return null
            }
        }

        data class Items(val amount: Int, val item: SkyblockId) : Reward {
            override fun mergeWith(other: Reward): Reward? {
                if (other is Items && other.item == item)
                    return Items(amount + other.amount, item)
                return null
            }
        }

        data class Unknown(val text: String) : Reward {
            override fun mergeWith(other: Reward): Reward? {
                return null
            }
        }
    }

    val expReward = "\\+(?<exp>$SHORT_NUMBER_FORMAT) (?<kind>[^ ]+) XP".toPattern()
    val coinReward = "\\+(?<amount>$SHORT_NUMBER_FORMAT) coins".toPattern()
    val itemReward = "(?:(?<amount>[0-9]+)x )?(?<name>.*)".toPattern()
    fun parseReward(string: String): Reward {
        expReward.useMatch<Unit>(string) {
            val exp = parseShortNumber(group("exp"))
            val kind = group("kind")
            return Reward.EXP(exp, kind)
        }
        coinReward.useMatch<Unit>(string) {
            val coins = parseShortNumber(group("amount"))
            return Reward.Coins(coins)
        }
        itemReward.useMatch(string) {
            val amount = group("amount")?.toIntOrNull() ?: 1
            val name = group("name")
            val item = ItemNameLookup.guessItemByName(name, false) ?: return@useMatch
            return Reward.Items(amount, item)
        }
        return Reward.Unknown(string)
    }

    @Subscribe
    fun onWorldClear(event: WorldReadyEvent) {
        lastClickedPig = null
        clickedPigs.clear()
    }

    @Subscribe
    fun onEntityClick(event: EntityInteractionEvent) {
        if (event.entity is PigEntity) {
            lastClickedPig = event.entity
        }
    }

    @Subscribe
    fun init(event: WorldReadyEvent) {
        PigCooldown.forceInit()
    }

    object PigCooldown : MoulConfigHud("anniversary_pig", TConfig.trackPigCooldown) {
        override fun shouldRender(): Boolean {
            return clickedPigs.isNotEmpty() && TConfig.enableShinyPigTracker
        }

        @Bind("pigs")
        fun getPigs() = clickedPigs

        class DisplayReward(val backedBy: Reward) {
            @Bind
            fun count(): String {
                return when (backedBy) {
                    is Reward.Coins -> backedBy.amount
                    is Reward.EXP -> backedBy.amount
                    is Reward.Items -> backedBy.amount
                    is Reward.Unknown -> 0
                }.toString()
            }

            val itemStack = if (backedBy is Reward.Items) {
	            SBItemStack(backedBy.item, backedBy.amount)
            } else {
	            SBItemStack(SkyblockId.NULL)
            }

            @OptIn(ExpensiveItemCacheApi::class)
			@Bind
            fun name(): String {
                return when (backedBy) {
                    is Reward.Coins -> "Coins"
                    is Reward.EXP -> backedBy.skill
                    is Reward.Items -> itemStack.asImmutableItemStack().name.string
                    is Reward.Unknown -> backedBy.text
                }
            }

            @Bind
            fun isKnown() = backedBy !is Reward.Unknown
        }

        @get:Bind("rewards")
        val rewards = ObservableList<DisplayReward>(mutableListOf())

    }

}
