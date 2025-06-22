package moe.nea.firmament.features.inventory.buttons

import com.mojang.brigadier.StringReader
import me.shedaniel.math.Dimension
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import kotlinx.serialization.Serializable
import net.minecraft.client.gui.DrawContext
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.ItemStackArgumentType
import net.minecraft.item.ItemStack
import net.minecraft.resource.featuretoggle.FeatureFlags
import net.minecraft.util.Identifier
import moe.nea.firmament.repo.ExpensiveItemCacheApi
import moe.nea.firmament.repo.ItemCache.asItemStack
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.collections.memoize
import moe.nea.firmament.util.mc.arbitraryUUID
import moe.nea.firmament.util.mc.createSkullItem
import moe.nea.firmament.util.render.drawGuiTexture

@Serializable
data class InventoryButton(
	var x: Int,
	var y: Int,
	var anchorRight: Boolean,
	var anchorBottom: Boolean,
	var icon: String? = "",
	var command: String? = "",
) {
	companion object {
		val itemStackParser by lazy {
			ItemStackArgumentType.itemStack(
				CommandRegistryAccess.of(
					MC.defaultRegistries,
					FeatureFlags.VANILLA_FEATURES
				)
			)
		}
		val dimensions = Dimension(18, 18)
		val getItemForName = ::getItemForName0.memoize(1024)
		@OptIn(ExpensiveItemCacheApi::class)
		fun getItemForName0(icon: String): ItemStack {
			val repoItem = RepoManager.getNEUItem(SkyblockId(icon))
			var itemStack = repoItem.asItemStack(idHint = SkyblockId(icon))
			if (repoItem == null) {
				when {
					icon.startsWith("skull:") -> {
						itemStack = createSkullItem(
							arbitraryUUID,
							"https://textures.minecraft.net/texture/${icon.substring("skull:".length)}"
						)
					}

					else -> {
						val giveSyntaxItem = if (icon.startsWith("/give") || icon.startsWith("give"))
							icon.split(" ", limit = 3).getOrNull(2) ?: icon
						else icon
						val componentItem =
							runCatching {
								itemStackParser.parse(StringReader(giveSyntaxItem)).createStack(1, false)
							}.getOrNull()
						if (componentItem != null)
							itemStack = componentItem
					}
				}
			}
			return itemStack
		}
	}

	fun render(context: DrawContext) {
		context.drawGuiTexture(
			0,
			0,
			0,
			dimensions.width,
			dimensions.height,
			Identifier.of("firmament:inventory_button_background")
		)
		context.drawItem(getItem(), 1, 1)
	}

	fun isValid() = !icon.isNullOrBlank() && !command.isNullOrBlank()

	fun getPosition(guiRect: Rectangle): Point {
		return Point(
			(if (anchorRight) guiRect.maxX else guiRect.minX) + x,
			(if (anchorBottom) guiRect.maxY else guiRect.minY) + y,
		)
	}

	fun getBounds(guiRect: Rectangle): Rectangle {
		return Rectangle(getPosition(guiRect), dimensions)
	}

	fun getItem(): ItemStack {
		return getItemForName(icon ?: "")
	}

}
