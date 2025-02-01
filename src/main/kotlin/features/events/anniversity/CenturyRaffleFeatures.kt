package moe.nea.firmament.features.events.anniversity

import java.util.Optional
import me.shedaniel.math.Color
import kotlin.jvm.optionals.getOrNull
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Style
import net.minecraft.util.Formatting
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.EntityRenderTintEvent
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SkyblockId
import moe.nea.firmament.util.render.TintedOverlayTexture
import moe.nea.firmament.util.skyBlockId
import moe.nea.firmament.util.skyblock.SkyBlockItems

object CenturyRaffleFeatures {
	object TConfig : ManagedConfig("centuryraffle", Category.EVENTS) {
		val highlightPlayersForSlice by toggle("highlight-cake-players") { true }
//		val highlightAllPlayers by toggle("highlight-all-cake-players") { true }
	}

	val cakeIcon = "⛃"

	val cakeColors = listOf(
		CakeTeam(SkyBlockItems.SLICE_OF_BLUEBERRY_CAKE, Formatting.BLUE),
		CakeTeam(SkyBlockItems.SLICE_OF_CHEESECAKE, Formatting.YELLOW),
		CakeTeam(SkyBlockItems.SLICE_OF_GREEN_VELVET_CAKE, Formatting.GREEN),
		CakeTeam(SkyBlockItems.SLICE_OF_RED_VELVET_CAKE, Formatting.RED),
		CakeTeam(SkyBlockItems.SLICE_OF_STRAWBERRY_SHORTCAKE, Formatting.LIGHT_PURPLE),
	)

	data class CakeTeam(
		val id: SkyblockId,
		val formatting: Formatting,
	) {
		val searchedTextRgb = formatting.colorValue!!
		val brightenedRgb = Color.ofOpaque(searchedTextRgb)//.brighter(2.0)
		val tintOverlay by lazy {
			TintedOverlayTexture().setColor(brightenedRgb)
		}
	}

	val sliceToColor = cakeColors.associateBy { it.id }

	@Subscribe
	fun onEntityRender(event: EntityRenderTintEvent) {
		if (!TConfig.highlightPlayersForSlice) return
		val requestedCakeTeam = sliceToColor[MC.stackInHand?.skyBlockId] ?: return
		// TODO: cache the requested color
		val player = event.entity as? PlayerEntity ?: return
		val cakeColor: Style = player.styledDisplayName.visit(
			{ style, text ->
				if (text == cakeIcon) Optional.of(style)
				else Optional.empty()
			}, Style.EMPTY).getOrNull() ?: return
		if (cakeColor.color?.rgb == requestedCakeTeam.searchedTextRgb) {
			event.renderState.overlayTexture_firmament = requestedCakeTeam.tintOverlay
		}
	}

}
