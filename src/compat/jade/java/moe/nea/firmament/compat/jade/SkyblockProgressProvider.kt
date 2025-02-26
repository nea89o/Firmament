package moe.nea.firmament.compat.jade

import java.util.function.BiConsumer
import snownee.jade.api.Accessor
import snownee.jade.api.view.ClientViewGroup
import snownee.jade.api.view.IClientExtensionProvider
import snownee.jade.api.view.ProgressView
import snownee.jade.api.view.ViewGroup
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class SkyblockProgressProvider : IClientExtensionProvider<ProgressView.Data, ProgressView> {
	// wtf does this do i think its for the little progress bar which breaks in mining fatigue mining system
	// but like this is just copied from the example plugin soo
	// TODO :3
	override fun getClientGroups(accessor: Accessor<*>, groups: List<ViewGroup<ProgressView.Data>>): List<ClientViewGroup<ProgressView>> {
		return ClientViewGroup.map(groups, ProgressView::read,
			BiConsumer { group: ViewGroup<ProgressView.Data>, clientGroup: ClientViewGroup<ProgressView> ->
				var view = clientGroup.views.first()
				view.style.color(-0x340000)
				view.text = Text.literal("e")
				view = clientGroup.views[1]
				view.style.color(-0xff3400)
				view.text = Text.literal("!!")
			})
	}

	override fun getUid(): Identifier? {
		return "progress".jadeId()
	}
}
