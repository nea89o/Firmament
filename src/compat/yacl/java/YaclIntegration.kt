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
import dev.isxander.yacl3.api.controller.ControllerBuilder
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder
import dev.isxander.yacl3.api.controller.StringControllerBuilder
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder
import dev.isxander.yacl3.gui.YACLScreen
import dev.isxander.yacl3.gui.tab.ListHolderWidget
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
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
import moe.nea.firmament.util.FirmFormatters


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
							.build())
				}
			}
			.build()
	}

	fun buildOptions(options: List<ManagedOption<*>>): Collection<Option<*>> =
		options.map { buildOption(it) }

	private fun <T : Any> buildOption(managedOption: ManagedOption<T>): Option<*> {
		val handler = managedOption.handler
		val binding = Binding.generic(managedOption.default(),
		                              managedOption::value,
		                              { managedOption.value = it; managedOption.element.save() })

		fun <T> createDefaultBinding(function: (Option<T>) -> ControllerBuilder<T>): Option.Builder<T> {
			return Option.createBuilder<T>()
				.name(managedOption.labelText)
				.description(OptionDescription.of(managedOption.labelDescription))
				.binding(binding as Binding<T>)
				.controller { function(it) }
		}
		when (handler) {
			is ClickHandler -> return ButtonOption.createBuilder()
				.name(managedOption.labelText)
				.action { t, u ->
					handler.runnable()
				}
				.build()

			is HudMetaHandler -> return ButtonOption.createBuilder()
				.name(managedOption.labelText)
				.action { t, u ->
					handler.openEditor(managedOption as ManagedOption<HudMeta>, t)
				}
				.build()

			is BooleanHandler -> return createDefaultBinding(TickBoxControllerBuilder::create).build()
			is StringHandler -> return createDefaultBinding(StringControllerBuilder::create).build()
			is IntegerHandler -> return createDefaultBinding {
				IntegerSliderControllerBuilder.create(it).range(handler.min, handler.max).step(1)
			}.build()

			is DurationHandler -> return Option.createBuilder<Double>()
				.name(managedOption.labelText)
				.binding((binding as Binding<Duration>).xmap({ it.toDouble(DurationUnit.SECONDS) }, { it.seconds }))
				.controller {
					DoubleSliderControllerBuilder.create(it)
						.formatValue { Text.literal(FirmFormatters.formatTimespan(it.seconds)) }
						.step(0.1)
						.range(handler.min.toDouble(DurationUnit.SECONDS), handler.max.toDouble(DurationUnit.SECONDS))
				}
				.build()

			is KeyBindingHandler -> return createDefaultBinding {
				KeybindingBuilder(it, managedOption as ManagedOption<SavedKeyBinding>)
			}.build()

			else -> return LabelOption.create(Text.literal("This option is currently unhandled for this config menu. Please report this as a bug."))
		}
	}


	fun buildConfig(): YetAnotherConfigLib {
		return YetAnotherConfigLib.createBuilder()
			.title(Text.literal("Firmament"))
			.categories(buildCategories())
			.build()
	}

	override val key: String
		get() = "yacl"

	override fun open(parent: Screen?): Screen {
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
