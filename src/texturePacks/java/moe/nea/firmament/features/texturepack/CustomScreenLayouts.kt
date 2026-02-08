package moe.nea.firmament.features.texturepack

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.gui.screens.inventory.HangingSignEditScreen
import net.minecraft.client.gui.screens.inventory.SignEditScreen
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimplePreparableReloadListener
import net.minecraft.util.profiling.ProfilerFiller
import net.minecraft.world.inventory.Slot
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.events.FinalizeResourceManagerEvent
import moe.nea.firmament.events.ScreenChangeEvent
import moe.nea.firmament.features.texturepack.CustomScreenLayouts.Alignment.CENTER
import moe.nea.firmament.features.texturepack.CustomScreenLayouts.Alignment.LEFT
import moe.nea.firmament.features.texturepack.CustomScreenLayouts.Alignment.RIGHT
import moe.nea.firmament.mixins.accessor.AccessorScreenHandler
import moe.nea.firmament.util.ErrorUtil.intoCatch
import moe.nea.firmament.util.IdentifierSerializer
import moe.nea.firmament.util.accessors.castAccessor

object CustomScreenLayouts : SimplePreparableReloadListener<List<CustomScreenLayouts.CustomScreenLayout>>() {

	@Serializable
	data class CustomScreenLayout(
		val predicates: Preds,
		val background: BackgroundReplacer? = null,
		val slots: List<SlotReplacer> = listOf(),
		val playerTitle: TitleReplacer? = null,
		val containerTitle: TitleReplacer? = null,
		val repairCostTitle: TitleReplacer? = null,
		val nameField: ComponentMover? = null,
		val signLines: List<ComponentMover>? = null,
	) {
		init {
			if (signLines != null)
				require(signLines.size == 4)
		}
	}

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
			val type = when (screen) {
				is AbstractContainerScreen<*> -> (screen.menu as AccessorScreenHandler).type_firmament?.let {
					BuiltInRegistries.MENU.getKey(it)
				}

				is HangingSignEditScreen -> Identifier.fromNamespaceAndPath("firmskyblock", "hanging_sign")
				is SignEditScreen -> Identifier.fromNamespaceAndPath("firmskyblock", "sign")
				else -> null
			}
			val typeMatches = screenType == null || type == screenType;
			return label.matches(screen.title) && typeMatches
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
		fun renderDirect(context: GuiGraphics) {
			context.blit(
				RenderPipelines.GUI_TEXTURED,
				this.texture,
				this.x, this.y,
				0F, 0F,
				this.width, this.height, this.width, this.height,
			)
		}

		fun renderGeneric(context: GuiGraphics, screen: AbstractContainerScreen<*>) {
			screen.castAccessor()
			val originalX: Int = (screen.width - screen.backgroundWidth_Firmament) / 2
			val originalY: Int = (screen.height - screen.backgroundHeight_Firmament) / 2
			val modifiedX = originalX + this.x
			val modifiedY = originalY + this.y
			val textureWidth = this.width
			val textureHeight = this.height
			context.blit(
				RenderPipelines.GUI_TEXTURED,
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
		val replacedText: Component? = replace?.let(Component::literal)

		fun replaceText(text: Component): Component {
			if (replacedText != null) return replacedText
			return text
		}

		fun replaceY(y: Int): Int {
			return this.y ?: y
		}

		fun replaceX(font: Font, text: Component, x: Int): Int {
			val baseX = this.x ?: x
			return baseX + when (this.align) {
				LEFT -> 0
				CENTER -> -font.width(text) / 2
				RIGHT -> -font.width(text)
			}
		}

		/**
		 * Not technically part of the package, but it does allow for us to later on seamlessly integrate a color option into this class as well
		 */
		fun replaceColor(text: Component, color: Int): Int {
			return CustomTextColors.mapTextColor(text, color)
		}
	}


	@Subscribe
	fun onStart(event: FinalizeResourceManagerEvent) {
		event.resourceManager.registerReloadListener(CustomScreenLayouts)
	}

	override fun prepare(
        manager: ResourceManager,
        profiler: ProfilerFiller
	): List<CustomScreenLayout> {
		val allScreenLayouts = manager.listResources(
			"overrides/screen_layout",
			{ it.path.endsWith(".json") && it.namespace == "firmskyblock" })
		val allParsedLayouts = allScreenLayouts.mapNotNull { (path, stream) ->
			Firmament.tryDecodeJsonFromStream<CustomScreenLayout>(stream.open())
				.intoCatch("Could not read custom screen layout from $path").orNull()
		}
		return allParsedLayouts
	}

	var customScreenLayouts = listOf<CustomScreenLayout>()

	override fun apply(
        prepared: List<CustomScreenLayout>,
        manager: ResourceManager,
        profiler: ProfilerFiller
	) {
		this.customScreenLayouts = prepared
	}

	@get:JvmStatic
	var activeScreenOverride = null as CustomScreenLayout?

	val DO_NOTHING_TEXT_REPLACER = TitleReplacer()

	@JvmStatic
	fun <T> getMover(selector: (CustomScreenLayout) -> (T?)) =
		activeScreenOverride?.let(selector)

	@JvmStatic
	fun getSignTextMover(index: Int) =
		getMover { it.signLines?.get(index) }

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

		val screen = event.new as? AbstractContainerScreen<*> ?: return
		val handler = screen.menu
		activeScreenOverride?.let { override ->
			override.slots.forEach { slotReplacer ->
				slotReplacer.move(handler.slots)
			}
		}
	}
}
