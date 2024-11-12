package moe.nea.firmament.compat.moulconfig

import com.google.auto.service.AutoService
import io.github.notenoughupdates.moulconfig.Config
import io.github.notenoughupdates.moulconfig.common.IMinecraft
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
import io.github.notenoughupdates.moulconfig.gui.editors.GuiOptionEditorText
import io.github.notenoughupdates.moulconfig.observer.GetSetter
import io.github.notenoughupdates.moulconfig.processor.ProcessedCategory
import io.github.notenoughupdates.moulconfig.processor.ProcessedOption
import java.lang.reflect.Type
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import net.minecraft.client.gui.screen.Screen
import moe.nea.firmament.gui.config.BooleanHandler
import moe.nea.firmament.gui.config.ClickHandler
import moe.nea.firmament.gui.config.DurationHandler
import moe.nea.firmament.gui.config.FirmamentConfigScreenProvider
import moe.nea.firmament.gui.config.HudMeta
import moe.nea.firmament.gui.config.HudMetaHandler
import moe.nea.firmament.gui.config.IntegerHandler
import moe.nea.firmament.gui.config.KeyBindingHandler
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.gui.config.ManagedOption
import moe.nea.firmament.gui.config.StringHandler
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
			wrapComponent(RowComponent(
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
			))
		}
	}

	init {
		register(BooleanHandler::class.java) { handler, option, categoryAccordionId, configObject ->
			object : ProcessedEditableOptionFirm<Boolean>(option, categoryAccordionId, configObject) {
				override fun createEditor(): GuiOptionEditor {
					return GuiOptionEditorBoolean(this, -1, configObject)
				}

				override fun get(): Any {
					return managedOption.value
				}

				override fun getType(): Type {
					return Boolean::class.java
				}

				override fun set(value: Any?): Boolean {
					managedOption.value = value as Boolean
					return true
				}
			}
		}
		register(StringHandler::class.java) { handler, option, categoryAccordionId, configObject ->
			object : ProcessedEditableOptionFirm<String>(option, categoryAccordionId, configObject) {
				override fun createEditor(): GuiOptionEditor {
					return GuiOptionEditorText(this)
				}

				override fun get(): Any {
					return managedOption.value
				}

				override fun getType(): Type {
					return String::class.java
				}

				override fun set(value: Any?): Boolean {
					managedOption.value = value as String
					return true
				}
			}
		}
		register(ClickHandler::class.java) { handler, option, categoryAccordionId, configObject ->
			object : ProcessedEditableOptionFirm<Unit>(option, categoryAccordionId, configObject) {
				override fun createEditor(): GuiOptionEditor {
					return GuiOptionEditorButton(this, -1, "Click", configObject)
				}

				override fun get(): Any {
					return Runnable { handler.runnable() }
				}

				override fun getType(): Type {
					return Runnable::class.java
				}

				override fun set(value: Any?): Boolean {
					ErrorUtil.softError("Trying to set a buttons data")
					return false
				}
			}
		}
		register(HudMetaHandler::class.java) { handler, option, categoryAccordionId, configObject ->
			object : ProcessedEditableOptionFirm<HudMeta>(option, categoryAccordionId, configObject) {
				override fun createEditor(): GuiOptionEditor {
					return GuiOptionEditorButton(this, -1, "Edit HUD", configObject)
				}

				override fun get(): Any {
					return Runnable {
						handler.openEditor(option, MC.screen!!)
					}
				}

				override fun getType(): Type {
					return Runnable::class.java
				}

				override fun set(value: Any?): Boolean {
					ErrorUtil.softError("Trying to assign to a hud meta")
					return false
				}
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

				override fun get(): Any {
					ErrorUtil.softError("Getting on a slider component")
					return Unit
				}

				override fun getType(): Type {
					return Nothing::class.java
				}

				override fun set(value: Any?): Boolean {
					ErrorUtil.softError("Setting on a slider component")
					return false
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

				override fun get(): Any {
					ErrorUtil.softError("Getting on a slider component")
					return Unit
				}

				override fun getType(): Type {
					return Nothing::class.java
				}

				override fun set(value: Any?): Boolean {
					ErrorUtil.softError("Setting on a slider component")
					return false
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

				override fun get(): Any {
					ErrorUtil.softError("Getting on a keybinding")
					return Unit
				}

				override fun getType(): Type {
					return Nothing::class.java
				}

				override fun set(value: Any?): Boolean {
					ErrorUtil.softError("Setting on a keybinding")
					return false
				}
			}
		}
	}

	override fun open(parent: Screen?): Screen {
		val configObject = object : Config() {
			override fun saveNow() {
				ManagedConfig.allManagedConfigs.getAll().forEach { it.save() }
			}

			override fun shouldAutoFocusSearchbar(): Boolean {
				return true
			}
		}
		val categories = ManagedConfig.Category.entries.map {
			val options = mutableListOf<ProcessedOptionFirm>()
			var nextAccordionId = 720
			it.configs.forEach { config ->
				val categoryAccordionId = nextAccordionId++
				options.add(object : ProcessedOptionFirm(-1, configObject) {
					override fun getDebugDeclarationLocation(): String {
						return "FirmamentConfig:$config.name"
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
		val editor = MoulConfigEditor(ProcessedCategory.collect(categories), configObject)
		return GuiElementWrapper(editor) // TODO : add parent support
	}

}
