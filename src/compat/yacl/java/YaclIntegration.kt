package moe.nea.firmament.compat.yacl

import com.google.auto.service.AutoService
import dev.isxander.yacl3.api.Binding
import dev.isxander.yacl3.api.ButtonOption
import dev.isxander.yacl3.api.ConfigCategory
import dev.isxander.yacl3.api.LabelOption
import dev.isxander.yacl3.api.Option
import dev.isxander.yacl3.api.OptionDescription
import dev.isxander.yacl3.api.OptionGroup
import dev.isxander.yacl3.api.YetAnotherConfigLib
import dev.isxander.yacl3.api.controller.ColorControllerBuilder
import dev.isxander.yacl3.api.controller.ControllerBuilder
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder
import dev.isxander.yacl3.api.controller.EnumControllerBuilder
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder
import dev.isxander.yacl3.api.controller.StringControllerBuilder
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder
import dev.isxander.yacl3.api.controller.ValueFormatter
import dev.isxander.yacl3.gui.YACLScreen
import dev.isxander.yacl3.gui.tab.ListHolderWidget
import io.github.notenoughupdates.moulconfig.ChromaColour
import java.awt.Color
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import moe.nea.firmament.gui.config.BooleanHandler
import moe.nea.firmament.gui.config.ChoiceHandler
import moe.nea.firmament.gui.config.ClickHandler
import moe.nea.firmament.gui.config.ColourHandler
import moe.nea.firmament.gui.config.DurationHandler
import moe.nea.firmament.gui.config.EnumRenderer
import moe.nea.firmament.gui.config.FirmamentConfigScreenProvider
import moe.nea.firmament.gui.config.HudMeta
import moe.nea.firmament.gui.config.HudMetaHandler
import moe.nea.firmament.gui.config.IntegerHandler
import moe.nea.firmament.gui.config.KeyBindingHandler
import moe.nea.firmament.gui.config.ManagedConfig
import moe.nea.firmament.gui.config.ManagedOption
import moe.nea.firmament.gui.config.StringHandler
import moe.nea.firmament.keybindings.SavedKeyBinding
import moe.nea.firmament.util.FirmFormatters
import moe.nea.firmament.util.getRGBAWithoutAnimation
import moe.nea.firmament.util.toChromaWithoutAnimation


@AutoService(FirmamentConfigScreenProvider::class)
class YaclIntegration : FirmamentConfigScreenProvider {
	fun buildCategories() =
		ManagedConfig.Category.entries
			.map(::buildCategory)

	private fun buildCategory(category: ManagedConfig.Category): ConfigCategory {
		return ConfigCategory.createBuilder()
			.name(category.labelText)
			.also { categoryB ->
				category.configs.forEach {
					categoryB.group(
						OptionGroup.createBuilder()
							.name(it.labelText)
							.options(buildOptions(it.sortedOptions))
							.build()
					)
				}
			}
			.build()
	}

	fun buildOptions(options: List<ManagedOption<*>>): Collection<Option<*>> =
		options.flatMap { buildOption(it) }

	private fun <T : Any> buildOption(managedOption: ManagedOption<T>): Collection<Option<*>> {
		val handler = managedOption.handler
		val binding = Binding.generic(
			managedOption.default(),
			managedOption::value,
			{ managedOption.value = it; managedOption.element.save() })

		fun <T> createDefaultBinding(function: (Option<T>) -> ControllerBuilder<T>): Option.Builder<T> {
			return Option.createBuilder<T>()
				.name(managedOption.labelText)
				.description(OptionDescription.of(managedOption.labelDescription))
				.binding(binding as Binding<T>)
				.controller { function(it) }
		}

		fun Option<out Any>.single() = listOf(this)
		fun ButtonOption.Builder.single() = build().single()
		fun Option.Builder<out Any>.single() = build().single()
		when (handler) {
			is ClickHandler -> return ButtonOption.createBuilder()
				.name(managedOption.labelText)
				.action { t, u ->
					handler.runnable()
				}
				.single()

			is HudMetaHandler -> return ButtonOption.createBuilder()
				.name(managedOption.labelText)
				.action { t, u ->
					handler.openEditor(managedOption as ManagedOption<HudMeta>, t)
				}
				.single()

			is ChoiceHandler<*> -> return createDefaultBinding {
				createChoiceBinding(handler as ChoiceHandler<*>, managedOption as ManagedOption<*>, it as Option<*>)
			}.single()

			is ColourHandler -> {
				managedOption as ManagedOption<ChromaColour>
				val colorBinding =
					Binding.generic(
						managedOption.default().getRGBAWithoutAnimation(),
						{ managedOption.value.getRGBAWithoutAnimation() },
						{
							managedOption.value =
								it.toChromaWithoutAnimation(managedOption.value.timeForFullRotationInMillis)
							managedOption.element.save()
						})
				val speedBinding =
					Binding.generic(
						managedOption.default().timeForFullRotationInMillis,
						{ managedOption.value.timeForFullRotationInMillis },
						{
							managedOption.value = managedOption.value.copy(timeForFullRotationInMillis = it)
							managedOption.element.save()
						}
					)

				return listOf(
					Option.createBuilder<Color>()
						.name(managedOption.labelText)
						.binding(colorBinding)
						.controller {
							ColorControllerBuilder.create(it)
								.allowAlpha(true)
						}
						.build(),
					Option.createBuilder<Int>()
						.name(managedOption.labelText)
						.binding(speedBinding)
						.controller { IntegerSliderControllerBuilder.create(it).range(0, 60_000).step(10) }
						.build(),
				)
			}

			is BooleanHandler -> return createDefaultBinding(TickBoxControllerBuilder::create).single()
			is StringHandler -> return createDefaultBinding(StringControllerBuilder::create).single()
			is IntegerHandler -> return createDefaultBinding {
				IntegerSliderControllerBuilder.create(it).range(handler.min, handler.max).step(1)
			}.single()

			is DurationHandler -> return Option.createBuilder<Double>()
				.name(managedOption.labelText)
				.binding((binding as Binding<Duration>).xmap({ it.toDouble(DurationUnit.SECONDS) }, { it.seconds }))
				.controller {
					DoubleSliderControllerBuilder.create(it)
						.formatValue { Text.literal(FirmFormatters.formatTimespan(it.seconds)) }
						.step(0.1)
						.range(handler.min.toDouble(DurationUnit.SECONDS), handler.max.toDouble(DurationUnit.SECONDS))
				}
				.single()

			is KeyBindingHandler -> return createDefaultBinding {
				KeybindingBuilder(it, managedOption as ManagedOption<SavedKeyBinding>)
			}.single()

			else -> return listOf(LabelOption.create(Text.literal("This option is currently unhandled for this config menu. Please report this as a bug.")))
		}
	}

	private enum class Sacrifice {}

	private fun createChoiceBinding(
		handler: ChoiceHandler<*>,
		managedOption: ManagedOption<*>,
		option: Option<*>
	): ControllerBuilder<Any> {
		val b = EnumControllerBuilder.create(option as Option<Sacrifice>)
		b.enumClass(handler.enumClass as Class<Sacrifice>)
		/**
		 * This is a function with E to avoid realizing the Sacrifice outside of a `X<E>` wrapper.
		 */
		fun <E : Enum<*>> makeValueFormatter(): ValueFormatter<E> {
			return ValueFormatter<E> {
				(handler.renderer as EnumRenderer<E>).getName(managedOption as ManagedOption<E>, it)
			}
		}
		b.formatValue(makeValueFormatter())
		return b as ControllerBuilder<Any>
	}


	fun buildConfig(): YetAnotherConfigLib {
		return YetAnotherConfigLib.createBuilder()
			.title(Text.literal("Firmament"))
			.categories(buildCategories())
			.build()
	}

	override val key: String
		get() = "yacl"

	override fun open(search: String?, parent: Screen?): Screen {
		return object : YACLScreen(buildConfig(), parent) {
			override fun setFocused(focused: Element?) {
				if (this.focused is KeybindingWidget &&
					focused is ListHolderWidget<*>
				) {
					return
				}
				super.setFocused(focused)
			}

			override fun shouldCloseOnEsc(): Boolean {
				if (focused is KeybindingWidget) {
					return false
				}
				return super.shouldCloseOnEsc()
			}
		}
	}

}
