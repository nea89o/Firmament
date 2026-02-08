package moe.nea.firmament.features.inventory.buttons

import com.mojang.brigadier.StringReader
import me.shedaniel.math.Dimension
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import kotlinx.serialization.Serializable
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.arguments.item.ItemArgument
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.resources.Identifier
import moe.nea.firmament.repo.ExpensiveItemCacheApi
import moe.nea.firmament.repo.ItemCache.asItemStack
import moe.nea.firmament.repo.ItemCache.isBroken
import moe.nea.firmament.repo.RepoManager
import moe.nea.firmament.util.ErrorUtil
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
	var isGigantic: Boolean = false,
) {

	val myDimension get() = if (isGigantic) bigDimension else dimensions

	companion object {
		val itemStackParser by lazy {
			ItemArgument.item(
				CommandBuildContext.simple(
					MC.defaultRegistries,
					FeatureFlags.VANILLA_SET
				)
			)
		}
		val dimensions = Dimension(18, 18)
		val gap = 2
		val bigDimension = Dimension(dimensions.width * 2 + gap, dimensions.height * 2 + gap)
		val getCustomItem = ::getCustomItem0.memoize(500)



		fun getCustomItem0(icon: String): ItemStack? {
			when {
				icon.startsWith("skull:") -> {
					return createSkullItem(
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
							itemStackParser.parse(StringReader(giveSyntaxItem)).createItemStack(1, false)
						}.getOrNull()
					return componentItem
				}
			}
		}

		@OptIn(ExpensiveItemCacheApi::class)
		fun getItemForName(icon: String): ItemStack {
			val repoItem = RepoManager.getNEUItem(SkyblockId(icon))
			var itemStack = repoItem.asItemStack(idHint = SkyblockId(icon))
			if (repoItem == null) {
				val customItem = getCustomItem(icon)
				if (customItem != null)
					itemStack = customItem
			}
			return itemStack
		}
	}

	fun render(context: GuiGraphics) {
		context.blitSprite(
			RenderPipelines.GUI_TEXTURED,
			Identifier.parse("firmament:inventory_button_background"),
			0,
			0,
			myDimension.width,
			myDimension.height,
		)
		if (isGigantic) {
			context.pose().pushMatrix()
			context.pose().translate(myDimension.width / 2F, myDimension.height / 2F)
			context.pose().scale(2F)
			context.renderItem(getItem(), -8, -8)
			context.pose().popMatrix()
		} else {
			context.renderItem(getItem(), 1, 1)
		}
	}

	fun isValid() = !icon.isNullOrBlank() && !command.isNullOrBlank()

	fun getPosition(guiRect: Rectangle): Point {
		return Point(
			(if (anchorRight) guiRect.maxX else guiRect.minX) + x,
			(if (anchorBottom) guiRect.maxY else guiRect.minY) + y,
		)
	}

	fun getBounds(guiRect: Rectangle): Rectangle {
		return Rectangle(getPosition(guiRect), myDimension)
	}

	fun getItem(): ItemStack {
		return getItemForName(icon ?: "")
	}

}
