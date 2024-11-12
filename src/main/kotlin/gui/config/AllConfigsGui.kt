package moe.nea.firmament.gui.config

import io.github.notenoughupdates.moulconfig.observer.ObservableList
import io.github.notenoughupdates.moulconfig.xml.Bind
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.MoulConfigUtils
import moe.nea.firmament.util.ScreenUtil.setScreenLater

object AllConfigsGui {
//
//	val allConfigs
//		get() = listOf(
//			RepoManager.Config
//		) + FeatureManager.allFeatures.mapNotNull { it.config }

	object ConfigConfig : ManagedConfig("configconfig", Category.META) {
		val enableYacl by toggle("enable-yacl") { false }
		val enableMoulConfig by toggle("enable-moulconfig") { true }
	}

	fun <T> List<T>.toObservableList(): ObservableList<T> = ObservableList(this)

	class CategoryMapping(val category: ManagedConfig.Category) {
		@get:Bind("configs")
		val configs = category.configs.map { EntryMapping(it) }.toObservableList()

		@Bind
		fun name() = category.labelText.string

		@Bind
		fun close() {
			MC.screen?.close()
		}

		class EntryMapping(val config: ManagedConfig) {
			@Bind
			fun name() = Text.translatable("firmament.config.${config.name}").string

			@Bind
			fun openEditor() {
				config.showConfigEditor(MC.screen)
			}
		}
	}

	class CategoryView {
		@get:Bind("categories")
		val categories = ManagedConfig.Category.entries
			.map { CategoryEntry(it) }
			.toObservableList()

		class CategoryEntry(val category: ManagedConfig.Category) {
			@Bind
			fun name() = category.labelText.string

			@Bind
			fun open() {
				MC.screen = MoulConfigUtils.loadScreen("config/category", CategoryMapping(category), MC.screen)
			}
		}
	}

	fun makeBuiltInScreen(parent: Screen? = null): Screen {
		return MoulConfigUtils.loadScreen("config/main", CategoryView(), parent)
	}

	fun makeScreen(parent: Screen? = null): Screen {
		val wantedKey = when {
			ConfigConfig.enableMoulConfig -> "moulconfig"
			ConfigConfig.enableYacl -> "yacl"
			else -> "builtin"
		}
		val provider = FirmamentConfigScreenProvider.providers.find { it.key == wantedKey }
			?: FirmamentConfigScreenProvider.providers.first()
		return provider.open(parent)
	}

	fun showAllGuis() {
		setScreenLater(makeScreen())
	}
}
