package moe.nea.firmament.gui.config

import net.minecraft.client.gui.screen.Screen
import moe.nea.firmament.util.compatloader.CompatLoader

interface FirmamentConfigScreenProvider {
	val key: String
	val isEnabled: Boolean get() = true

	fun open(search: String?, parent: Screen?): Screen

	companion object : CompatLoader<FirmamentConfigScreenProvider>(FirmamentConfigScreenProvider::class) {
		val providers by lazy {
			allValidInstances
				.filter { it.isEnabled }
				.sortedWith(
					Comparator
						.comparing<FirmamentConfigScreenProvider, Boolean>({ it.key == "builtin" })
						.reversed()
						.then(Comparator.comparing({ it.key }))
				).toList()
		}
	}
}
