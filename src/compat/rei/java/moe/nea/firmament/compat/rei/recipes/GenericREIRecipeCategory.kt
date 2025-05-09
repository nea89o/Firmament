package moe.nea.firmament.compat.rei.recipes

import io.github.moulberry.repo.data.NEURecipe
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.util.EntryStacks
import net.minecraft.text.Text
import moe.nea.firmament.compat.rei.REIRecipeLayouter
import moe.nea.firmament.compat.rei.neuDisplayGeneratorWithItem
import moe.nea.firmament.repo.SBItemStack
import moe.nea.firmament.repo.recipes.GenericRecipeRenderer

class GenericREIRecipeCategory<T : NEURecipe>(
	val renderer: GenericRecipeRenderer<T>,
) : DisplayCategory<GenericRecipe<T>> {
	private val dynamicGenerator =
		neuDisplayGeneratorWithItem<GenericRecipe<T>, T>(renderer.typ) { item, recipe ->
			GenericRecipe(
				recipe,
				item,
				categoryIdentifier
			)
		}

	private val categoryIdentifier = CategoryIdentifier.of<GenericRecipe<T>>(renderer.identifier)
	override fun getCategoryIdentifier(): CategoryIdentifier<GenericRecipe<T>> {
		return categoryIdentifier
	}

	override fun getDisplayHeight(): Int {
		return renderer.displayHeight
	}

	override fun getTitle(): Text? {
		return renderer.title
	}

	override fun getIcon(): Renderer? {
		return EntryStacks.of(renderer.icon)
	}

	override fun setupDisplay(display: GenericRecipe<T>, bounds: Rectangle): List<Widget> {
		val layouter = REIRecipeLayouter()
		layouter.container.add(Widgets.createRecipeBase(bounds))
		renderer.render(display.neuRecipe, bounds, layouter, display.sourceItem)
		return layouter.container
	}

	fun registerDynamicGenerator(registry: DisplayRegistry) {
		registry.registerDisplayGenerator(categoryIdentifier, dynamicGenerator)
	}
}

class GenericRecipe<T : NEURecipe>(
	override val neuRecipe: T,
	val sourceItem: SBItemStack?,
	val id: CategoryIdentifier<GenericRecipe<T>>
) : SBRecipe() {
	override fun getCategoryIdentifier(): CategoryIdentifier<*>? {
		return id
	}
}
