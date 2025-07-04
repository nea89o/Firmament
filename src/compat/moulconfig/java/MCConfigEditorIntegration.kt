package moe.nea.firmament.compat.moulconfig

import com.google.auto.service.AutoService
import io.github.notenoughupdates.moulconfig.ChromaColour
import io.github.notenoughupdates.moulconfig.Config
import io.github.notenoughupdates.moulconfig.DescriptionRendereringBehaviour
import io.github.notenoughupdates.moulconfig.Social
import io.github.notenoughupdates.moulconfig.common.IMinecraft
import io.github.notenoughupdates.moulconfig.common.MyResourceLocation
import io.github.notenoughupdates.moulconfig.gui.GuiComponent
import io.github.notenoughupdates.moulconfig.gui.GuiElementWrapper
import io.github.notenoughupdates.moulconfig.gui.GuiOptionEditor
import io.github.notenoughupdates.moulconfig.gui.HorizontalAlign
import io.github.notenoughupdates.moulconfig.gui.MoulConfigEditor
import io.github.notenoughupdates.moulconfig.gui.VerticalAlign
import io.github.notenoughupdates.moulconfig.gui.component.AlignComponent
import io.github.notenoughupdates.moulconfig.gui.component.RowComponent
import io.github.notenoughupdates.moulconfig.gui.component.SliderComponent
import io.github.notenoughupdates.moulconfig.gui.component.TextComponent
import io.github.notenoughupdates.moulconfig.gui.editors.ComponentEditor
import io.github.notenoughupdates.moulconfig.gui.editors.GuiOptionEditorAccordion
import io.github.notenoughupdates.moulconfig.gui.editors.GuiOptionEditorBoolean
import io.github.notenoughupdates.moulconfig.gui.editors.GuiOptionEditorButton
import io.github.notenoughupdates.moulconfig.gui.editors.GuiOptionEditorColour
import io.github.notenoughupdates.moulconfig.gui.editors.GuiOptionEditorDropdown
import io.github.notenoughupdates.moulconfig.gui.editors.GuiOptionEditorText
import io.github.notenoughupdates.moulconfig.observer.GetSetter
import io.github.notenoughupdates.moulconfig.processor.ProcessedCategory
import io.github.notenoughupdates.moulconfig.processor.ProcessedOption
import java.lang.reflect.Type
import java.net.URI
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import net.minecraft.client.gui.screen.Screen
import net.minecraft.util.Identifier
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.Util
import moe.nea.firmament.Firmament
import moe.nea.firmament.gui.config.AllConfigsGui
import moe.nea.firmament.gui.config.BooleanHandler
import moe.nea.firmament.gui.config.ChoiceHandler
import moe.nea.firmament.gui.config.ClickHandler
import moe.nea.firmament.gui.config.ColourHandler
import moe.nea.firmament.gui.config.DurationHandler
import moe.nea.firmament.gui.config.FirmamentConfigScreenProvider
import moe.nea.firmament.gui.config.HudMeta
import moe.nea.firmament.gui.config.HudMetaHandler
import moe.nea.firmament.gui.config.IntegerHandler
import moe.nea.firmament.gui.config.KeyBindingHandler
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.gui.config.ManagedOption
import moe.nea.firmament.gui.config.StringHandler
import moe.nea.firmament.gui.toMoulConfig
import moe.nea.firmament.keybindings.SavedKeyBinding
import moe.nea.firmament.util.ErrorUtil
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.MoulConfigUtils.xmap

@AutoService(FirmamentConfigScreenProvider::class)
class MCConfigEditorIntegration : FirmamentConfigScreenProvider {
	override val key: String
		get() = "moulconfig"

	val handlers: MutableMap<Class<out ManagedConfig.OptionHandler<*>>, ((ManagedConfig.OptionHandler<*>, ManagedOption<*>, accordionId: Int, configObject: Config) -> ProcessedEditableOptionFirm<*>)> =
		mutableMapOf()

	fun <T : Any, H : ManagedConfig.OptionHandler<T>> register(
		handlerClass: Class<H>,
		transform: (H, ManagedOption<T>, accordionId: Int, configObject: Config) -> ProcessedEditableOptionFirm<T>
	) {
		handlers[handlerClass] =
			transform as ((ManagedConfig.OptionHandler<*>, ManagedOption<*>, accordionId: Int, configObject: Config) -> ProcessedEditableOptionFirm<*>)
	}

	fun <T : Any> getHandler(
		option: ManagedOption<T>,
		accordionId: Int,
		configObject: Config
	): ProcessedEditableOptionFirm<*> {
		val transform = handlers[option.handler.javaClass]
			?: error("Could not transform ${option.handler}") // TODO: replace with soft error and an error config element
		return transform.invoke(option.handler, option, accordionId, configObject) as ProcessedEditableOptionFirm<T>
	}

	class CustomSliderEditor<T>(
		option: ProcessedOption,
		setter: GetSetter<T>,
		fromT: (T) -> Float,
		toT: (Float) -> T,
		minValue: T, maxValue: T,
		minStep: Float,
		formatter: (T) -> String,
	) : ComponentEditor(option) {
		override fun getDelegate(): GuiComponent {
			return delegateI
		}

		val mappedSetter = setter.xmap(fromT, toT)

		private val delegateI by lazy {
			wrapComponent(
				RowComponent(
					AlignComponent(
						TextComponent(
							IMinecraft.instance.defaultFontRenderer,
							{ formatter(setter.get()) },
							25,
							TextComponent.TextAlignment.CENTER, false, false
						),
						GetSetter.constant(HorizontalAlign.CENTER),
						GetSetter.constant(VerticalAlign.CENTER)
					),
					SliderComponent(
						mappedSetter,
						fromT(minValue),
						fromT(maxValue),
						minStep,
						40
					)
				)
			)
		}
	}

	fun <T> helpRegisterChoice() where  T : Enum<T>, T : StringIdentifiable {
		register(ChoiceHandler::class.java as Class<ChoiceHandler<T>>) { handler, option, categoryAccordionId, configObject ->
			object : ProcessedEditableOptionFirm<T>(option, categoryAccordionId, configObject) {
				override fun createEditor(): GuiOptionEditor {
					return GuiOptionEditorDropdown(
						this,
						handler.universe.map { handler.renderer.getName(option, it).string }.toTypedArray()
					)
				}

				override fun toT(any: Any?): T? {
					return handler.universe[any as Int]
				}

				override fun getType(): Type {
					return Int::class.java
				}

				override fun fromT(t: T): Any {
					return t.ordinal
				}
			}
		}
	}

	init {
		helpRegisterChoice<Nothing>()
		register(BooleanHandler::class.java) { handler, option, categoryAccordionId, configObject ->
			object : ProcessedEditableOptionFirm<Boolean>(option, categoryAccordionId, configObject) {
				override fun createEditor(): GuiOptionEditor {
					return GuiOptionEditorBoolean(this, -1, configObject)
				}

				override fun toT(any: Any?): Boolean? {
					return any as Boolean
				}

				override fun getType(): Type {
					return Boolean::class.java
				}

				override fun fromT(t: Boolean): Any {
					return t
				}
			}
		}
		register(StringHandler::class.java) { handler, option, categoryAccordionId, configObject ->
			object : ProcessedEditableOptionFirm<String>(option, categoryAccordionId, configObject) {
				override fun createEditor(): GuiOptionEditor {
					return GuiOptionEditorText(this)
				}

				override fun getType(): Type {
					return String::class.java
				}

				override fun fromT(t: String): Any {
					return t
				}

				override fun toT(any: Any?): String? {
					return any as String
				}
			}
		}
		register(ColourHandler::class.java) { handler, option, accordionId, configObject ->
			object : ProcessedEditableOptionFirm<ChromaColour>(option, accordionId, configObject) {
				override fun fromT(t: ChromaColour): Any {
					return t
				}

				override fun toT(any: Any?): ChromaColour? {
					return any as ChromaColour?
				}

				override fun createEditor(): GuiOptionEditor {
					return GuiOptionEditorColour(this)
				}

				override fun getType(): Type? {
					return ChromaColour::class.java
				}
			}

		}
		register(ClickHandler::class.java) { handler, option, categoryAccordionId, configObject ->
			object : ProcessedEditableOptionFirm<Unit>(option, categoryAccordionId, configObject) {
				override fun createEditor(): GuiOptionEditor {
					return GuiOptionEditorButton(this, -1, "Click", configObject)
				}

				override fun toT(any: Any?): Unit? {
					return null
				}

				override fun fromT(t: Unit): Any {
					return Runnable { handler.runnable() }
				}

				override fun getType(): Type {
					return Runnable::class.java
				}
			}
		}
		register(HudMetaHandler::class.java) { handler, option, categoryAccordionId, configObject ->
			object : ProcessedEditableOptionFirm<HudMeta>(option, categoryAccordionId, configObject) {
				override fun createEditor(): GuiOptionEditor {
					return GuiOptionEditorButton(this, -1, "Edit HUD", configObject)
				}

				override fun fromT(t: HudMeta): Any {
					return Runnable {
						handler.openEditor(option, MC.screen!!)
					}
				}


				override fun getType(): Type {
					return Runnable::class.java
				}

				override fun toT(any: Any?): HudMeta? = null
			}
		}
		register(DurationHandler::class.java) { handler, option, categoryAccordionId, configObject ->
			object : ProcessedEditableOptionFirm<Duration>(option, categoryAccordionId, configObject) {
				override fun createEditor(): GuiOptionEditor {
					return CustomSliderEditor(
						this,
						option,
						{ it.toDouble(DurationUnit.SECONDS).toFloat() },
						{ it.toDouble().seconds },
						handler.min,
						handler.max,
						0.1F,
						FirmFormatters::formatTimespan
					)
				}

				override fun toT(any: Any?): Duration? = null
				override fun fromT(t: Duration): Any {
					ErrorUtil.softError("Getting on a slider component")
					return Unit
				}

				override fun getType(): Type {
					return Nothing::class.java
				}

			}
		}
		register(IntegerHandler::class.java) { handler, option, categoryAccordionId, configObject ->
			object : ProcessedEditableOptionFirm<Int>(option, categoryAccordionId, configObject) {
				override fun createEditor(): GuiOptionEditor {
					return CustomSliderEditor(
						this,
						option,
						{ it.toFloat() },
						{ it.toInt() },
						handler.min,
						handler.max,
						1F,
						Integer::toString
					)
				}

				override fun toT(any: Any?): Int? = null
				override fun fromT(t: Int): Any {
					ErrorUtil.softError("Getting on a slider component")
					return Unit
				}

				override fun getType(): Type {
					return Nothing::class.java
				}
			}
		}
		register(KeyBindingHandler::class.java) { handler, option, categoryAccordionId, configObject ->
			object : ProcessedEditableOptionFirm<SavedKeyBinding>(option, categoryAccordionId, configObject) {
				override fun createEditor(): GuiOptionEditor {
					return object : ComponentEditor(this) {
						val button = wrapComponent(handler.createButtonComponent(option))
						override fun getDelegate(): GuiComponent {
							return button
						}
					}
				}

				override fun toT(any: Any?): SavedKeyBinding? {
					return null
				}

				override fun getType(): Type {
					return Nothing::class.java
				}

				override fun fromT(t: SavedKeyBinding): Any {
					ErrorUtil.softError("Cannot get a keybinding editor")
					return Unit
				}
			}
		}
	}

	val configObject = object : Config() {
		override fun saveNow() {
			ManagedConfig.allManagedConfigs.getAll().forEach { it.save() }
		}

		override fun shouldAutoFocusSearchbar(): Boolean {
			return true
		}

		override fun getTitle(): String {
			return "Firmament ${Firmament.version.friendlyString}"
		}

		@Deprecated("Deprecated in java")
		override fun executeRunnable(runnableId: Int) {
			if (runnableId >= 0)
				ErrorUtil.softError("Executed runnable $runnableId")
		}

		override fun getDescriptionBehaviour(option: ProcessedOption?): DescriptionRendereringBehaviour {
			return DescriptionRendereringBehaviour.EXPAND_PANEL
		}

		fun mkSocial(name: String, identifier: Identifier, link: String) = object : Social() {
			override fun onClick() {
				Util.getOperatingSystem().open(URI(link))
			}

			override fun getTooltip(): List<String> {
				return listOf(name)
			}

			override fun getIcon(): MyResourceLocation {
				return identifier.toMoulConfig()
			}
		}

		private val socials = listOf<Social>(
			mkSocial(
				"Discord", Firmament.identifier("textures/socials/discord.png"),
				Firmament.modContainer.metadata.contact.get("discord").get()
			),
			mkSocial(
				"Source Code", Firmament.identifier("textures/socials/git.png"),
				Firmament.modContainer.metadata.contact.get("sources").get()
			),
			mkSocial(
				"Modrinth", Firmament.identifier("textures/socials/modrinth.png"),
				Firmament.modContainer.metadata.contact.get("modrinth").get()
			),
		)

		override fun getSocials(): List<Social> {
			return socials
		}
	}
	val categories = ManagedConfig.Category.entries.map {
		val options = mutableListOf<ProcessedOptionFirm>()
		var nextAccordionId = 720
		it.configs.forEach { config ->
			val categoryAccordionId = nextAccordionId++
			options.add(object : ProcessedOptionFirm(-1, configObject) {
				override fun getDebugDeclarationLocation(): String {
					return "FirmamentConfig:${config.name}"
				}

				override fun getName(): String {
					return config.labelText.string
				}

				override fun getDescription(): String {
					return "Missing description"
				}

				override fun get(): Any {
					return Unit
				}

				override fun getType(): Type {
					return Unit.javaClass
				}

				override fun set(value: Any?): Boolean {
					return false
				}

				override fun createEditor(): GuiOptionEditor {
					return GuiOptionEditorAccordion(this, categoryAccordionId)
				}
			})
			config.allOptions.forEach { (key, option) ->
				val processedOption = getHandler(option, categoryAccordionId, configObject)
				options.add(processedOption)
			}
		}

		return@map ProcessedCategoryFirm(it, options)
	}

	override fun open(search: String?, parent: Screen?): Screen {
		val editor = MoulConfigEditor(ProcessedCategory.collect(categories), configObject)
		if (search != null)
			editor.search(search)
		editor.setWide(AllConfigsGui.ConfigConfig.enableWideMC)
		return GuiElementWrapper(editor) // TODO : add parent support
	}

}
