package moe.nea.firmament.compat.rei.recipes

import io.github.moulberry.repo.data.NEUKatUpgradeRecipe
import io.github.notenoughupdates.moulconfig.common.IMinecraft
import io.github.notenoughupdates.moulconfig.gui.GuiComponent
import io.github.notenoughupdates.moulconfig.gui.GuiImmediateContext
import io.github.notenoughupdates.moulconfig.gui.MouseEvent
import io.github.notenoughupdates.moulconfig.gui.component.SliderComponent
import io.github.notenoughupdates.moulconfig.observer.GetSetter
import io.github.notenoughupdates.moulconfig.observer.Property
import io.github.notenoughupdates.moulconfig.platform.MoulConfigRenderContext
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.common.category.CategoryIdentifier
import kotlin.time.Duration.Companion.seconds
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.world.item.Items
import net.minecraft.network.chat.Component
import moe.nea.firmament.Firmament
import moe.nea.firmament.compat.rei.SBItemEntryDefinition
import moe.nea.firmament.repo.PetData
import moe.nea.firmament.repo.SBItemStack
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SkyblockId

class SBKatRecipe(override val neuRecipe: NEUKatUpgradeRecipe) : SBRecipe() {
	override fun getCategoryIdentifier(): CategoryIdentifier<*> = Category.categoryIdentifier

	object Category : DisplayCategory<SBKatRecipe> {
		override fun getCategoryIdentifier(): CategoryIdentifier<SBKatRecipe> =
			CategoryIdentifier.of(Firmament.MOD_ID, "kat_recipe")

		override fun getTitle(): Component = Component.literal("Kat Pet Upgrade")
		override fun getDisplayHeight(): Int {
			return 100
		}

		override fun getIcon(): Renderer = SBItemEntryDefinition.getPassthrough(Items.BONE)
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
					Component.literal("")
				).centered()
				val outputLevelLabelCenter = Point(bounds.maxX - 30 + 8, bounds.minY + 25)
				val outputLevelLabel = Widgets.createLabel(
					outputLevelLabelCenter,
					Component.literal("")
				).centered()
				val coinStack = SBItemStack(SkyblockId.COINS, recipe.coins.toInt())
				levelValue.whenChanged { oldValue, newValue ->
					if (oldValue.toInt() == newValue.toInt()) return@whenChanged
					val oldInput = inputStack.getPetData() ?: return@whenChanged
					val newInput = PetData.forLevel(oldInput.petId, oldInput.rarity, newValue.toInt())
					inputStack.setPetData(newInput)
					val oldOutput = outputStack.getPetData() ?: return@whenChanged
					val newOutput = PetData(oldOutput.rarity, oldOutput.petId, newInput.exp)
					outputStack.setPetData(newOutput)
					inputLevelLabel.message = Component.literal(newInput.levelData.currentLevel.toString())
					inputLevelLabel.bounds.location = Point(
						inputLevelLabelCenter.x - MC.font.width(inputLevelLabel.message) / 2,
						inputLevelLabelCenter.y
					)
					outputLevelLabel.message = Component.literal(newOutput.levelData.currentLevel.toString())
					outputLevelLabel.bounds.location = Point(
						outputLevelLabelCenter.x - MC.font.width(outputLevelLabel.message) / 2,
						outputLevelLabelCenter.y
					)
					coinStack.setStackSize((recipe.coins * (1 - 0.3 * newValue / 100)).toInt())
				}
				levelValue.set(1F)
				add(Widgets.createRecipeBase(bounds))
				add(
					wrapWidget(
						Rectangle(
							bounds.centerX - slider.width / 2,
							bounds.maxY - 30,
							slider.width,
							slider.height
						),
						slider
					)
				)
				add(
					Widgets.withTooltip(
						Widgets.createArrow(Point(bounds.centerX - arrowWidth / 2, bounds.minY + 40)),
						Component.literal("Upgrade time: " + FirmFormatters.formatTimespan(recipe.seconds.seconds))
					)
				)

				add(Widgets.createResultSlotBackground(Point(bounds.maxX - 30, bounds.minY + 40)))
				add(inputLevelLabel)
				add(outputLevelLabel)
				add(
					Widgets.createSlot(Point(bounds.maxX - 30, bounds.minY + 40)).markOutput().disableBackground()
						.entry(SBItemEntryDefinition.getEntry(outputStack))
				)
				add(
					Widgets.createSlot(Point(bounds.minX + 30 - 18 + 5, bounds.minY + 40)).markInput()
						.entry(SBItemEntryDefinition.getEntry(inputStack))
				)

				val allInputs = recipe.items.map { SBItemEntryDefinition.getEntry(it) } +
					listOf(SBItemEntryDefinition.getEntry(coinStack))
				for ((index, item) in allInputs.withIndex()) {
					add(
						Widgets.createSlot(
							Point(
								bounds.centerX + index * 20 - allInputs.size * 18 / 2 - (allInputs.size - 1) * 2 / 2,
								bounds.minY + 20
							)
						)
							.markInput()
							.entry(item)
					)
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

		override fun children(): List<GuiEventListener> {
			return listOf()
		}

		override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
			context.pose().pushMatrix()
			context.pose().translate(bounds.minX.toFloat(), bounds.minY.toFloat())
			component.render(
				GuiImmediateContext(
					MoulConfigRenderContext(context),
					bounds.minX, bounds.minY,
					bounds.width, bounds.height,
					mouseX - bounds.minX, mouseY - bounds.minY,
					mouseX, mouseY,
					mouseX.toFloat(), mouseY.toFloat()
				)
			)
			context.pose().popMatrix()
		}

		override fun mouseMoved(mouseX: Double, mouseY: Double) {
			val mouseXInt = mouseX.toInt()
			val mouseYInt = mouseY.toInt()
			component.mouseEvent(
				MouseEvent.Move(0F, 0F),
				GuiImmediateContext(
					IMinecraft.INSTANCE.provideTopLevelRenderContext(),
					bounds.minX, bounds.minY,
					bounds.width, bounds.height,
					mouseXInt - bounds.minX, mouseYInt - bounds.minY,
					mouseXInt, mouseYInt,
					mouseX.toFloat(), mouseY.toFloat()
				)
			)
		}

		override fun mouseClicked(event: MouseButtonEvent, isDoubleClick: Boolean): Boolean {
			val mouseXInt = event.x.toInt()
			val mouseYInt = event.y.toInt()
			return component.mouseEvent(
				MouseEvent.Click(event.button(), true),
				GuiImmediateContext(
					IMinecraft.INSTANCE.provideTopLevelRenderContext(),
					bounds.minX, bounds.minY,
					bounds.width, bounds.height,
					mouseXInt - bounds.minX, mouseYInt - bounds.minY,
					mouseXInt, mouseYInt,
					event.x.toFloat(), event.y.toFloat()
				)
			)
		}

		override fun mouseReleased(event: MouseButtonEvent): Boolean {
			val mouseXInt = event.x.toInt()
			val mouseYInt = event.y.toInt()
			return component.mouseEvent(
				MouseEvent.Click(event.button(), false),
				GuiImmediateContext(
					IMinecraft.INSTANCE.provideTopLevelRenderContext(),
					bounds.minX, bounds.minY,
					bounds.width, bounds.height,
					mouseXInt - bounds.minX, mouseYInt - bounds.minY,
					mouseXInt, mouseYInt,
					event.x.toFloat(), event.y.toFloat()
				)
			)
		}

		override fun mouseDragged(event: MouseButtonEvent, mouseX: Double, mouseY: Double): Boolean {
			val mouseXInt = event.x.toInt()
			val mouseYInt = event.y.toInt()
			return component.mouseEvent(
				MouseEvent.Move(0f, 0f),
				GuiImmediateContext(
					IMinecraft.INSTANCE.provideTopLevelRenderContext(),
					bounds.minX, bounds.minY,
					bounds.width, bounds.height,
					mouseXInt - bounds.minX, mouseYInt - bounds.minY,
					mouseXInt, mouseYInt,
					event.x.toFloat(), event.y.toFloat()
				)
			)

		}

		override fun mouseScrolled(
			mouseX: Double,
			mouseY: Double,
			horizontalAmount: Double,
			verticalAmount: Double
		): Boolean {
			val mouseXInt = mouseX.toInt()
			val mouseYInt = mouseY.toInt()
			return component.mouseEvent(
				MouseEvent.Scroll(verticalAmount.toFloat()),
				GuiImmediateContext(
					IMinecraft.INSTANCE.provideTopLevelRenderContext(),
					bounds.minX, bounds.minY,
					bounds.width, bounds.height,
					mouseXInt - bounds.minX, mouseYInt - bounds.minY,
					mouseXInt, mouseYInt,
					mouseX.toFloat(), mouseY.toFloat()
				)
			)
		}
	}
}
