
package moe.nea.firmament.features.events.carnival

import io.github.notenoughupdates.moulconfig.observer.ObservableList
import io.github.notenoughupdates.moulconfig.platform.ModernItemStack
import io.github.notenoughupdates.moulconfig.xml.Bind
import java.util.UUID
import net.minecraft.block.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.ClickEvent
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.WorldAccess
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.commands.thenExecute
import moe.nea.firmament.events.AttackBlockEvent
import moe.nea.firmament.events.CommandEvent
import moe.nea.firmament.events.EntityUpdateEvent
import moe.nea.firmament.events.ProcessChatEvent
import moe.nea.firmament.events.WorldReadyEvent
import moe.nea.firmament.events.WorldRenderLastEvent
import moe.nea.firmament.features.debug.DebugLogger
import moe.nea.firmament.util.LegacyFormattingCode
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.MoulConfigUtils
import moe.nea.firmament.util.ScreenUtil
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.mc.createSkullItem
import moe.nea.firmament.util.render.RenderInWorldContext
import moe.nea.firmament.util.setSkyBlockFirmamentUiId
import moe.nea.firmament.util.skyBlockId
import moe.nea.firmament.util.useMatch

object MinesweeperHelper {
    val sandBoxLow = BlockPos(-112, 72, -11)
    val sandBoxHigh = BlockPos(-106, 72, -5)
    val boardSize = Pair(sandBoxHigh.x - sandBoxLow.x, sandBoxHigh.z - sandBoxLow.z)

    val gameStartMessage = "[NPC] Carnival Pirateman: Good luck, matey!"
    val gameEndMessage = "Fruit Digging"
    val bombPattern = "MINES! There (are|is) (?<bombCount>[0-8]) bombs? hidden nearby\\.".toPattern()
    val startGameQuestion = "[NPC] Carnival Pirateman: Would ye like to do some Fruit Digging?"


    enum class Piece(
        @get:Bind("fruitName")
        val fruitName: String,
        val points: Int,
        val specialAbility: String,
        val totalPerBoard: Int,
        val textureHash: String,
        val fruitColor: LegacyFormattingCode,
    ) {
        COCONUT("Coconut",
                200,
                "Prevents a bomb from exploding next turn",
                3,
                "10ceb1455b471d016a9f06d25f6e468df9fcf223e2c1e4795b16e84fcca264ee",
                LegacyFormattingCode.DARK_PURPLE),
        APPLE("Apple",
              100,
              "Gains 100 points for each apple dug up",
              8,
              "17ea278d6225c447c5943d652798d0bbbd1418434ce8c54c54fdac79994ddd6c",
              LegacyFormattingCode.GREEN),
        WATERMELON("Watermelon",
                   100,
                   "Blows up an adjacent fruit for half the points",
                   4,
                   "efe4ef83baf105e8dee6cf03dfe7407f1911b3b9952c891ae34139560f2931d6",
                   LegacyFormattingCode.DARK_BLUE),
        DURIAN("Durian",
               800,
               "Halves the points earned in the next turn",
               2,
               "ac268d36c2c6047ffeec00124096376b56dbb4d756a55329363a1b27fcd659cd",
               LegacyFormattingCode.DARK_PURPLE),
        MANGO("Mango",
              300,
              "Just an ordinary fruit",
              10,
              "f363a62126a35537f8189343a22660de75e810c6ac004a7d3da65f1c040a839",
              LegacyFormattingCode.GREEN),
        DRAGON_FRUIT("Dragonfruit",
                     1200,
                     "Halves the points earned in the next turn",
                     1,
                     "3cc761bcb0579763d9b8ab6b7b96fa77eb6d9605a804d838fec39e7b25f95591",
                     LegacyFormattingCode.LIGHT_PURPLE),
        POMEGRANATE("Pomegranate",
                    200,
                    "Grants an extra 50% more points in the next turn",
                    4,
                    "40824d18079042d5769f264f44394b95b9b99ce689688cc10c9eec3f882ccc08",
                    LegacyFormattingCode.DARK_BLUE),
        CHERRY("Cherry",
               200,
               "The second cherry grants 300 bonus points",
               2,
               "c92b099a62cd2fbf8ada09dec145c75d7fda4dc57b968bea3a8fa11e37aa48b2",
               LegacyFormattingCode.DARK_PURPLE),
        BOMB("Bomb",
             -1,
             "Destroys nearby fruit",
             15,
             "a76a2811d1e176a07b6d0a657b910f134896ce30850f6e80c7c83732d85381ea",
             LegacyFormattingCode.DARK_RED),
        RUM("Rum",
            -1,
            "Stops your dowsing ability for one turn",
            5,
            "407b275d28b927b1bf7f6dd9f45fbdad2af8571c54c8f027d1bff6956fbf3c16",
            LegacyFormattingCode.YELLOW),
        ;

        val textureUrl = "http://textures.minecraft.net/texture/$textureHash"
        val itemStack = createSkullItem(UUID.randomUUID(), textureUrl)
            .setSkyBlockFirmamentUiId("MINESWEEPER_$name")

        @Bind
        fun getIcon() = ModernItemStack.of(itemStack)

        @Bind
        fun pieceLabel() = fruitColor.formattingCode + fruitName

        @Bind
        fun boardLabel() = "§a$totalPerBoard§7/§rboard"

        @Bind("description")
        fun getDescription() = buildString {
            append(specialAbility)
            if (points >= 0) {
                append(" Default points: $points.")
            }
        }
    }

    object TutorialScreen {
        @get:Bind("pieces")
        val pieces = ObservableList(Piece.entries.toList().reversed())

        @get:Bind("modes")
        val modes = ObservableList(DowsingMode.entries.toList())
    }

    enum class DowsingMode(
        val itemType: Item,
        @get:Bind("feature")
        val feature: String,
        @get:Bind("description")
        val description: String,
    ) {
        MINES(Items.IRON_SHOVEL, "Bomb detection", "Tells you how many bombs are near the block"),
        ANCHOR(Items.DIAMOND_SHOVEL, "Lowest fruit", "Shows you which block nearby contains the lowest scoring fruit"),
        TREASURE(Items.GOLDEN_SHOVEL, "Highest fruit", "Tells you which kind of fruit is the highest scoring nearby"),
        ;

        @Bind("itemType")
        fun getItemStack() = ModernItemStack.of(ItemStack(itemType))

        companion object {
            val id = SkyblockId("CARNIVAL_SHOVEL")
            fun fromItem(itemStack: ItemStack): DowsingMode? {
                if (itemStack.skyBlockId != id) return null
                return DowsingMode.entries.find { it.itemType == itemStack.item }
            }
        }
    }

    data class BoardPosition(
        val x: Int,
        val y: Int
    ) {
        fun toBlockPos() = BlockPos(sandBoxLow.x + x, sandBoxLow.y, sandBoxLow.z + y)

        fun getBlock(world: WorldAccess) = world.getBlockState(toBlockPos()).block
        fun isUnopened(world: WorldAccess) = getBlock(world) == Blocks.SAND
        fun isOpened(world: WorldAccess) = getBlock(world) == Blocks.SANDSTONE
        fun isScorched(world: WorldAccess) = getBlock(world) == Blocks.SANDSTONE_STAIRS

        companion object {
            fun fromBlockPos(blockPos: BlockPos): BoardPosition? {
                if (blockPos.y != sandBoxLow.y) return null
                val x = blockPos.x - sandBoxLow.x
                val y = blockPos.z - sandBoxLow.z
                if (x < 0 || x >= boardSize.first) return null
                if (y < 0 || y >= boardSize.second) return null
                return BoardPosition(x, y)
            }
        }
    }

    data class GameState(
        val nearbyBombs: MutableMap<BoardPosition, Int> = mutableMapOf(),
        val knownBombPositions: MutableSet<BoardPosition> = mutableSetOf(),
        var lastClickedPosition: BoardPosition? = null,
        var lastDowsingMode: DowsingMode? = null,
    )

    var gameState: GameState? = null
    val log = DebugLogger("minesweeper")

    @Subscribe
    fun onCommand(event: CommandEvent.SubCommand) {
        event.subcommand("minesweepertutorial") {
            thenExecute {
                ScreenUtil.setScreenLater(MoulConfigUtils.loadScreen("carnival/minesweeper_tutorial",
                                                                     TutorialScreen,
                                                                     null))
            }
        }
    }

    @Subscribe
    fun onWorldChange(event: WorldReadyEvent) {
        gameState = null
    }

    @Subscribe
    fun onChat(event: ProcessChatEvent) {
        if (CarnivalFeatures.TConfig.displayTutorials && event.unformattedString == startGameQuestion) {
            MC.sendChat(Text.translatable("firmament.carnival.tutorial.minesweeper").styled {
                it.withClickEvent(ClickEvent.RunCommand("/firm minesweepertutorial"))
            })
        }
        if (!CarnivalFeatures.TConfig.enableBombSolver) {
            gameState = null // TODO: replace this which a watchable property
            return
        }
        if (event.unformattedString == gameStartMessage) {
            gameState = GameState()
            log.log { "Game started" }
        }
        if (event.unformattedString.trim() == gameEndMessage) {
            gameState = null // TODO: add a loot tracker maybe? probably not, i dont think people care
            log.log { "Finished game" }
        }
        val gs = gameState ?: return
        bombPattern.useMatch(event.unformattedString) {
            val bombCount = group("bombCount").toInt()
            log.log { "Marking ${gs.lastClickedPosition} as having $bombCount nearby" }
            val pos = gs.lastClickedPosition ?: return
            gs.nearbyBombs[pos] = bombCount
        }
    }

    @Subscribe
    fun onMobChange(event: EntityUpdateEvent) {
        val gs = gameState ?: return
        if (event !is EntityUpdateEvent.TrackedDataUpdate) return
        // TODO: listen to state
    }

    @Subscribe
    fun onBlockClick(event: AttackBlockEvent) {
        val gs = gameState ?: return
        val boardPosition = BoardPosition.fromBlockPos(event.blockPos)
        log.log { "Breaking block at ${event.blockPos} ($boardPosition)" }
        gs.lastClickedPosition = boardPosition
        gs.lastDowsingMode = DowsingMode.fromItem(event.player.mainHandStack)
    }

    @Subscribe
    fun onRender(event: WorldRenderLastEvent) {
        val gs = gameState ?: return
        RenderInWorldContext.renderInWorld(event) {
            for ((pos, bombCount) in gs.nearbyBombs) {
                this.text(pos.toBlockPos().up().toCenterPos(), Text.literal("§a$bombCount \uD83D\uDCA3"))
            }
        }
    }


}
