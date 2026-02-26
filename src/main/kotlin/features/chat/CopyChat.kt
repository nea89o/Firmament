package moe.nea.firmament.features.chat

import org.joml.Matrix3x2f
import net.minecraft.client.gui.ActiveTextCollector
import net.minecraft.client.gui.TextAlignment
import net.minecraft.client.gui.render.state.GuiTextRenderState
import net.minecraft.network.chat.Component
import net.minecraft.util.ARGB
import net.minecraft.util.FormattedCharSequence
import moe.nea.firmament.util.MC.font
import moe.nea.firmament.util.data.Config
import moe.nea.firmament.util.data.ManagedConfig
import moe.nea.firmament.util.reconstitute


object CopyChat {
	val identifier: String
		get() = "copy-chat"

	@Config
	object TConfig : ManagedConfig(identifier, Category.CHAT) {
		val copyChat by toggle("copy-chat") { false }
	}

	class HoveredTextLineCollector(val testX: Int, val testY: Int) : ActiveTextCollector {
		var parameters: ActiveTextCollector.Parameters = ActiveTextCollector.Parameters(Matrix3x2f())

		override fun defaultParameters(): ActiveTextCollector.Parameters {
			return parameters
		}

		override fun defaultParameters(parameters: ActiveTextCollector.Parameters) {
			this.parameters = parameters
		}

		override fun accept(
			textAlignment: TextAlignment,
			i: Int,
			j: Int,
			parameters: ActiveTextCollector.Parameters,
			formattedCharSequence: FormattedCharSequence
		) {
			val k = textAlignment.calculateLeft(i, font, formattedCharSequence)
			if (GuiTextRenderState(
					font,
					formattedCharSequence,
					parameters.pose(),
					k,
					j,
					ARGB.white(parameters.opacity()),
					0,
					true,
					true,
					parameters.scissor()
				)
					.bounds()!!
					.containsPoint(testX, testY)
			) {
				this.result = formattedCharSequence
			}
		}

		var result: FormattedCharSequence? = null

		override fun acceptScrolling(
			component: Component,
			i: Int,
			j: Int,
			k: Int,
			l: Int,
			m: Int,
			parameters: ActiveTextCollector.Parameters
		) {
			val n = font.width(component)
			val o = 9
			this.defaultScrollingHelper(component, i, j, k, l, m, n, o, parameters)
		}
	}

	fun orderedTextToString(orderedText: FormattedCharSequence): String {
		return orderedText.reconstitute().string
	}
}
