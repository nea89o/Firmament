
package moe.nea.firmament.rei.recipes

import io.github.moulberry.repo.data.NEUKatUpgradeRecipe
import io.github.notenoughupdates.moulconfig.common.IMinecraft
import io.github.notenoughupdates.moulconfig.gui.GuiComponent
import io.github.notenoughupdates.moulconfig.gui.GuiImmediateContext
import io.github.notenoughupdates.moulconfig.gui.MouseEvent
import io.github.notenoughupdates.moulconfig.gui.component.SliderComponent
import io.github.notenoughupdates.moulconfig.observer.GetSetter
import io.github.notenoughupdates.moulconfig.observer.Property
import io.github.notenoughupdates.moulconfig.platform.ModernRenderContext
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.util.EntryStacks
import kotlin.time.Duration.Companion.seconds
import net.minecraft.block.Blocks
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.item.Items
import net.minecraft.text.Text
import moe.nea.firmament.Firmament
import moe.nea.firmament.rei.PetData
import moe.nea.firmament.rei.SBItemEntryDefinition
import moe.nea.firmament.rei.SBItemStack
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SkyblockId

class SBKatRecipe(override val neuRecipe: NEUKatUpgradeRecipe) : SBRecipe() {
    override fun getCategoryIdentifier(): CategoryIdentifier<*> = Category.categoryIdentifier

    object Category : DisplayCategory<SBKatRecipe> {
        override fun getCategoryIdentifier(): CategoryIdentifier<SBKatRecipe> =
            CategoryIdentifier.of(Firmament.MOD_ID, "kat_recipe")

        override fun getTitle(): Text = Text.literal("Kat Pet Upgrade")
        override fun getDisplayHeight(): Int {
            return 100
        }

        override fun getIcon(): Renderer = EntryStacks.of(Items.BONE)
        override fun setupDisplay(display: SBKatRecipe, bounds: Rectangle): List<Widget> {
            return buildList {
                val arrowWidth = 24
                val recipe = display.neuRecipe
                val levelValue = Property.upgrade(GetSetter.floating(0F))
                val slider = SliderComponent(levelValue, 1F, 100F, 1f, 100)
                val outputStack = SBItemStack(SkyblockId(recipe.output.itemId))
                val inputStack = SBItemStack(SkyblockId(recipe.input.itemId))
                val inputLevelLabelCenter = Point(bounds.minX + 30 - 18 + 5 + 8, bounds.minY + 25)
                val inputLevelLabel = Widgets.createLabel(
                    inputLevelLabelCenter,
                    Text.literal("")).centered()
                val outputLevelLabelCenter = Point(bounds.maxX - 30 + 8, bounds.minY + 25)
                val outputLevelLabel = Widgets.createLabel(
                    outputLevelLabelCenter,
                    Text.literal("")).centered()
                val coinStack = SBItemStack(SkyblockId.COINS, recipe.coins.toInt())
                levelValue.whenChanged { oldValue, newValue ->
                    if (oldValue.toInt() == newValue.toInt()) return@whenChanged
                    val oldInput = inputStack.getPetData() ?: return@whenChanged
                    val newInput = PetData.forLevel(oldInput.petId, oldInput.rarity, newValue.toInt())
                    inputStack.setPetData(newInput)
                    val oldOutput = outputStack.getPetData() ?: return@whenChanged
                    val newOutput = PetData(oldOutput.rarity, oldOutput.petId, newInput.exp)
                    outputStack.setPetData(newOutput)
                    inputLevelLabel.message = Text.literal(newInput.levelData.currentLevel.toString())
                    inputLevelLabel.bounds.location = Point(
                        inputLevelLabelCenter.x - MC.font.getWidth(inputLevelLabel.message) / 2,
                        inputLevelLabelCenter.y)
                    outputLevelLabel.message = Text.literal(newOutput.levelData.currentLevel.toString())
                    outputLevelLabel.bounds.location = Point(
                        outputLevelLabelCenter.x - MC.font.getWidth(outputLevelLabel.message) / 2,
                        outputLevelLabelCenter.y)
                    coinStack.setStackSize((recipe.coins * (1 - 0.3 * newValue / 100)).toInt())
                }
                levelValue.set(1F)
                add(Widgets.createRecipeBase(bounds))
                add(wrapWidget(Rectangle(bounds.centerX - slider.width / 2,
                                         bounds.maxY - 30,
                                         slider.width,
                                         slider.height),
                               slider))
                add(Widgets.withTooltip(
                    Widgets.createArrow(Point(bounds.centerX - arrowWidth / 2, bounds.minY + 40)),
                    Text.literal("Upgrade time: " + FirmFormatters.formatTimespan(recipe.seconds.seconds))))

                add(Widgets.createResultSlotBackground(Point(bounds.maxX - 30, bounds.minY + 40)))
                add(inputLevelLabel)
                add(outputLevelLabel)
                add(Widgets.createSlot(Point(bounds.maxX - 30, bounds.minY + 40)).markOutput().disableBackground()
                        .entry(SBItemEntryDefinition.getEntry(outputStack)))
                add(Widgets.createSlot(Point(bounds.minX + 30 - 18 + 5, bounds.minY + 40)).markInput()
                        .entry(SBItemEntryDefinition.getEntry(inputStack)))

                val allInputs = recipe.items.map { SBItemEntryDefinition.getEntry(it) } +
                    listOf(SBItemEntryDefinition.getEntry(coinStack))
                for ((index, item) in allInputs.withIndex()) {
                    add(Widgets.createSlot(
                        Point(bounds.centerX + index * 20 - allInputs.size * 18 / 2 - (allInputs.size - 1) * 2 / 2,
                              bounds.minY + 20))
                            .markInput()
                            .entry(item))
                }
            }
        }
    }
}

fun wrapWidget(bounds: Rectangle, component: GuiComponent): Widget {
    return object : WidgetWithBounds() {
        override fun getBounds(): Rectangle {
            return bounds
        }

        override fun children(): List<Element> {
            return listOf()
        }

        override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
            context.matrices.push()
            context.matrices.translate(bounds.minX.toFloat(), bounds.minY.toFloat(), 0F)
            component.render(
                GuiImmediateContext(
                    ModernRenderContext(context),
                    bounds.minX, bounds.minY,
                    bounds.width, bounds.height,
                    mouseX - bounds.minX, mouseY - bounds.minY,
                    mouseX, mouseY,
                    mouseX.toFloat(), mouseY.toFloat()
                ))
            context.matrices.pop()
        }

        override fun mouseMoved(mouseX: Double, mouseY: Double) {
            val mouseXInt = mouseX.toInt()
            val mouseYInt = mouseY.toInt()
            component.mouseEvent(MouseEvent.Move(0F, 0F),
                                 GuiImmediateContext(
                                     IMinecraft.instance.provideTopLevelRenderContext(),
                                     bounds.minX, bounds.minY,
                                     bounds.width, bounds.height,
                                     mouseXInt - bounds.minX, mouseYInt - bounds.minY,
                                     mouseXInt, mouseYInt,
                                     mouseX.toFloat(), mouseY.toFloat()
                                 ))
        }

        override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
            val mouseXInt = mouseX.toInt()
            val mouseYInt = mouseY.toInt()
            return component.mouseEvent(MouseEvent.Click(button, true),
                                        GuiImmediateContext(
                                            IMinecraft.instance.provideTopLevelRenderContext(),
                                            bounds.minX, bounds.minY,
                                            bounds.width, bounds.height,
                                            mouseXInt - bounds.minX, mouseYInt - bounds.minY,
                                            mouseXInt, mouseYInt,
                                            mouseX.toFloat(), mouseY.toFloat()
                                        ))
        }

        override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
            val mouseXInt = mouseX.toInt()
            val mouseYInt = mouseY.toInt()
            return component.mouseEvent(MouseEvent.Click(button, false),
                                        GuiImmediateContext(
                                            IMinecraft.instance.provideTopLevelRenderContext(),
                                            bounds.minX, bounds.minY,
                                            bounds.width, bounds.height,
                                            mouseXInt - bounds.minX, mouseYInt - bounds.minY,
                                            mouseXInt, mouseYInt,
                                            mouseX.toFloat(), mouseY.toFloat()
                                        ))
        }

        override fun mouseDragged(
            mouseX: Double,
            mouseY: Double,
            button: Int,
            deltaX: Double,
            deltaY: Double
        ): Boolean {
            val mouseXInt = mouseX.toInt()
            val mouseYInt = mouseY.toInt()
            return component.mouseEvent(MouseEvent.Move(deltaX.toFloat(), deltaY.toFloat()),
                                        GuiImmediateContext(
                                            IMinecraft.instance.provideTopLevelRenderContext(),
                                            bounds.minX, bounds.minY,
                                            bounds.width, bounds.height,
                                            mouseXInt - bounds.minX, mouseYInt - bounds.minY,
                                            mouseXInt, mouseYInt,
                                            mouseX.toFloat(), mouseY.toFloat()
                                        ))

        }

        override fun mouseScrolled(
            mouseX: Double,
            mouseY: Double,
            horizontalAmount: Double,
            verticalAmount: Double
        ): Boolean {
            val mouseXInt = mouseX.toInt()
            val mouseYInt = mouseY.toInt()
            return component.mouseEvent(MouseEvent.Scroll(verticalAmount.toFloat()),
                                        GuiImmediateContext(
                                            IMinecraft.instance.provideTopLevelRenderContext(),
                                            bounds.minX, bounds.minY,
                                            bounds.width, bounds.height,
                                            mouseXInt - bounds.minX, mouseYInt - bounds.minY,
                                            mouseXInt, mouseYInt,
                                            mouseX.toFloat(), mouseY.toFloat()
                                        ))
        }
    }
}
