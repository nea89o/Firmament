package moe.nea.firmament.features.texturepack

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.render.RenderLayer
import net.minecraft.registry.Registries
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.SinglePreparationResourceReloader
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.profiler.Profiler
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.FinalizeResourceManagerEvent
import moe.nea.firmament.events.ScreenChangeEvent
import moe.nea.firmament.features.texturepack.CustomScreenLayouts.Alignment.CENTER
import moe.nea.firmament.features.texturepack.CustomScreenLayouts.Alignment.LEFT
import moe.nea.firmament.features.texturepack.CustomScreenLayouts.Alignment.RIGHT
import moe.nea.firmament.mixins.accessor.AccessorHandledScreen
import moe.nea.firmament.util.ErrorUtil.intoCatch
import moe.nea.firmament.util.IdentifierSerializer

object CustomScreenLayouts : SinglePreparationResourceReloader<List<CustomScreenLayouts.CustomScreenLayout>>() {

	@Serializable
	data class CustomScreenLayout(
		val predicates: Preds,
		val background: BackgroundReplacer? = null,
		val slots: List<SlotReplacer> = listOf(),
		val playerTitle: TitleReplacer? = null,
		val containerTitle: TitleReplacer? = null,
		val repairCostTitle: TitleReplacer? = null,
		val nameField: ComponentMover? = null,
	)

	@Serializable
	data class ComponentMover(
		val x: Int,
		val y: Int,
		val width: Int? = null,
		val height: Int? = null,
	)

	@Serializable
	data class Preds(
		val label: StringMatcher,
		@Serializable(with = IdentifierSerializer::class)
		val screenType: Identifier? = null,
	) {
		fun matches(screen: Screen): Boolean {
			// TODO: does this deserve the restriction to handled screen
			val s = screen as? HandledScreen<*>? ?: return false
			val typeMatches = screenType == null || s.screenHandler.type.equals(Registries.SCREEN_HANDLER
				.get(screenType));

			return label.matches(s.title) && typeMatches
		}
	}

	@Serializable
	data class BackgroundReplacer(
		@Serializable(with = IdentifierSerializer::class)
		val texture: Identifier,
		// TODO: allow selectively still rendering some components (recipe button, trade backgrounds, furnace flame progress, arrows)
		val x: Int,
		val y: Int,
		val width: Int,
		val height: Int,
	) {
		fun renderGeneric(context: DrawContext, screen: HandledScreen<*>) {
			screen as AccessorHandledScreen
			val originalX: Int = (screen.width - screen.backgroundWidth_Firmament) / 2
			val originalY: Int = (screen.height - screen.backgroundHeight_Firmament) / 2
			val modifiedX = originalX + this.x
			val modifiedY = originalY + this.y
			val textureWidth = this.width
			val textureHeight = this.height
			context.drawTexture(
				RenderLayer::getGuiTextured,
				this.texture,
				modifiedX,
				modifiedY,
				0.0f,
				0.0f,
				textureWidth,
				textureHeight,
				textureWidth,
				textureHeight
			)

		}
	}

	@Serializable
	data class SlotReplacer(
		// TODO: override getRecipeBookButtonPos as well
		// TODO: is this index or id (i always forget which one is duplicated per inventory)
		val index: Int,
		val x: Int,
		val y: Int,
	) {
		fun move(slots: List<Slot>) {
			val slot = slots.getOrNull(index) ?: return
			slot.x = x
			slot.y = y
		}
	}

	@Serializable
	enum class Alignment {
		@SerialName("left")
		LEFT,

		@SerialName("center")
		CENTER,

		@SerialName("right")
		RIGHT
	}

	@Serializable
	data class TitleReplacer(
		val x: Int? = null,
		val y: Int? = null,
		val align: Alignment = Alignment.LEFT,
		val replace: String? = null
	) {
		@Transient
		val replacedText: Text? = replace?.let(Text::literal)

		fun replaceText(text: Text): Text {
			if (replacedText != null) return replacedText
			return text
		}

		fun replaceY(y: Int): Int {
			return this.y ?: y
		}

		fun replaceX(font: TextRenderer, text: Text, x: Int): Int {
			val baseX = this.x ?: x
			return baseX + when (this.align) {
				LEFT -> 0
				CENTER -> -font.getWidth(text) / 2
				RIGHT -> -font.getWidth(text)
			}
		}

		/**
		 * Not technically part of the package, but it does allow for us to later on seamlessly integrate a color option into this class as well
		 */
		fun replaceColor(text: Text, color: Int): Int {
			return CustomTextColors.mapTextColor(text, color)
		}
	}


	@Subscribe
	fun onStart(event: FinalizeResourceManagerEvent) {
		event.resourceManager.registerReloader(CustomScreenLayouts)
	}

	override fun prepare(
		manager: ResourceManager,
		profiler: Profiler
	): List<CustomScreenLayout> {
		val allScreenLayouts = manager.findResources(
			"overrides/screen_layout",
			{ it.path.endsWith(".json") && it.namespace == "firmskyblock" })
		val allParsedLayouts = allScreenLayouts.mapNotNull { (path, stream) ->
			Firmament.tryDecodeJsonFromStream<CustomScreenLayout>(stream.inputStream)
				.intoCatch("Could not read custom screen layout from $path").orNull()
		}
		return allParsedLayouts
	}

	var customScreenLayouts = listOf<CustomScreenLayout>()

	override fun apply(
		prepared: List<CustomScreenLayout>,
		manager: ResourceManager?,
		profiler: Profiler?
	) {
		this.customScreenLayouts = prepared
	}

	@get:JvmStatic
	var activeScreenOverride = null as CustomScreenLayout?

	val DO_NOTHING_TEXT_REPLACER = TitleReplacer()

	@JvmStatic
	fun <T>getMover(selector: (CustomScreenLayout)-> (T?)) =
		activeScreenOverride?.let(selector)

	@JvmStatic
	fun getTextMover(selector: (CustomScreenLayout) -> (TitleReplacer?)) =
		getMover(selector) ?: DO_NOTHING_TEXT_REPLACER

	@Subscribe
	fun onScreenOpen(event: ScreenChangeEvent) {
		if (!CustomSkyBlockTextures.TConfig.allowLayoutChanges) {
			activeScreenOverride = null
			return
		}
		activeScreenOverride = event.new?.let { screen ->
			customScreenLayouts.find { it.predicates.matches(screen) }
		}

		val screen = event.new as? HandledScreen<*> ?: return
		val handler = screen.screenHandler
		activeScreenOverride?.let { override ->
			override.slots.forEach { slotReplacer ->
				slotReplacer.move(handler.slots)
			}
		}
	}
}
