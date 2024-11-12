package moe.nea.firmament.gui.config

import net.minecraft.client.gui.screen.Screen
import moe.nea.firmament.util.compatloader.CompatLoader

interface FirmamentConfigScreenProvider {
	val key: String
	val isEnabled: Boolean get() = true

	fun open(parent: Screen?): Screen

	companion object : CompatLoader<FirmamentConfigScreenProvider>(FirmamentConfigScreenProvider::class) {
		val providers by lazy {
			allValidInstances
				.filter { it.isEnabled }
				.sortedWith(Comparator.comparing(
					{ it.key },
					Comparator<String> { left, right ->
						if (left == "builtin") return@Comparator -1
						if (right == "builtin") return@Comparator 1
						return@Comparator left.compareTo(right)
					})).toList()
		}
	}
}
