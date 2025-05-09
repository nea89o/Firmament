package moe.nea.firmament.compat.rei

import io.github.moulberry.repo.data.NEUForgeRecipe
import io.github.moulberry.repo.data.NEUKatUpgradeRecipe
import io.github.moulberry.repo.data.NEUMobDropRecipe
import io.github.moulberry.repo.data.NEUNpcShopRecipe
import io.github.moulberry.repo.data.NEURecipe
import java.util.Optional
import me.shedaniel.rei.api.client.registry.display.DynamicDisplayGenerator
import me.shedaniel.rei.api.client.view.ViewSearchBuilder
import me.shedaniel.rei.api.common.display.Display
import me.shedaniel.rei.api.common.entry.EntryStack
import moe.nea.firmament.compat.rei.recipes.SBKatRecipe
import moe.nea.firmament.compat.rei.recipes.SBMobDropRecipe
import moe.nea.firmament.compat.rei.recipes.SBShopRecipe
import moe.nea.firmament.repo.EssenceRecipeProvider
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.repo.SBItemStack


val SkyblockMobDropRecipeDynamicGenerator =
	neuDisplayGenerator<SBMobDropRecipe, NEUMobDropRecipe> { SBMobDropRecipe(it) }
val SkyblockShopRecipeDynamicGenerator =
	neuDisplayGenerator<SBShopRecipe, NEUNpcShopRecipe> { SBShopRecipe(it) }
val SkyblockKatRecipeDynamicGenerator =
	neuDisplayGenerator<SBKatRecipe, NEUKatUpgradeRecipe> { SBKatRecipe(it) }

inline fun <D : Display, reified T : NEURecipe> neuDisplayGenerator(crossinline mapper: (T) -> D) =
	neuDisplayGeneratorWithItem<D, T> { _, it -> mapper(it) }

inline fun <D : Display, reified T : NEURecipe> neuDisplayGeneratorWithItem(crossinline mapper: (SBItemStack, T) -> D) =
	neuDisplayGeneratorWithItem(T::class.java, mapper)
inline fun <D : Display, T : NEURecipe> neuDisplayGeneratorWithItem(
	filter: Class<T>,
	crossinline mapper: (SBItemStack, T) -> D) =
	object : DynamicDisplayGenerator<D> {
		override fun getRecipeFor(entry: EntryStack<*>): Optional<List<D>> {
			if (entry.type != SBItemEntryDefinition.type) return Optional.empty()
			val item = entry.castValue<SBItemStack>()
			val recipes = RepoManager.getRecipesFor(item.skyblockId)
			val craftingRecipes = recipes.filterIsInstance<T>(filter)
			return Optional.of(craftingRecipes.map { mapper(item, it) })
		}

		override fun generate(builder: ViewSearchBuilder): Optional<List<D>> {
			return Optional.empty() // TODO: allows searching without blocking getRecipeFor
		}

		override fun getUsageFor(entry: EntryStack<*>): Optional<List<D>> {
			if (entry.type != SBItemEntryDefinition.type) return Optional.empty()
			val item = entry.castValue<SBItemStack>()
			val recipes = RepoManager.getUsagesFor(item.skyblockId)
			val craftingRecipes = recipes.filterIsInstance<T>(filter)
			return Optional.of(craftingRecipes.map { mapper(item, it) })
		}
	}
