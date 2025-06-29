package moe.nea.firmament.util.mc

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.Optional
import org.jetbrains.annotations.TestOnly
import net.minecraft.client.gui.hud.PlayerListHud
import net.minecraft.nbt.NbtOps
import net.minecraft.scoreboard.Team
import net.minecraft.text.Text
import net.minecraft.text.TextCodecs
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.commands.thenExecute
import moe.nea.firmament.commands.thenLiteral
import moe.nea.firmament.events.CommandEvent
import moe.nea.firmament.events.TickEvent
import moe.nea.firmament.features.debug.DeveloperFeatures
import moe.nea.firmament.features.debug.ExportedTestConstantMeta
import moe.nea.firmament.mixins.accessor.AccessorPlayerListHud
import moe.nea.firmament.util.ClipboardUtils
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.intoOptional
import moe.nea.firmament.util.mc.SNbtFormatter.Companion.toPrettyString

object MCTabListAPI {

	fun PlayerListHud.cast() = this as AccessorPlayerListHud

	@Subscribe
	fun onTick(event: TickEvent) {
		_currentTabList = null
	}

	@Subscribe
	fun devCommand(event: CommandEvent.SubCommand) {
		event.subcommand(DeveloperFeatures.DEVELOPER_SUBCOMMAND) {
			thenLiteral("copytablist") {
				thenExecute {
					currentTabList.body.forEach {
						MC.sendChat(Text.literal(TextCodecs.CODEC.encodeStart(NbtOps.INSTANCE, it).orThrow.toString()))
					}
					var compound = CurrentTabList.CODEC.encodeStart(NbtOps.INSTANCE, currentTabList).orThrow
					compound = ExportedTestConstantMeta.SOURCE_CODEC.encode(
						ExportedTestConstantMeta.current,
						NbtOps.INSTANCE,
						compound
					).orThrow
					ClipboardUtils.setTextContent(
						compound.toPrettyString()
					)
				}
			}
		}
	}

	@get:TestOnly
	@set:TestOnly
	var _currentTabList: CurrentTabList? = null

	val currentTabList get() = _currentTabList ?: getTabListNow().also { _currentTabList = it }

	data class CurrentTabList(
		val header: Optional<Text>,
		val footer: Optional<Text>,
		val body: List<Text>,
	) {
		companion object {
			val CODEC: Codec<CurrentTabList> = RecordCodecBuilder.create {
				it.group(
					TextCodecs.CODEC.optionalFieldOf("header").forGetter(CurrentTabList::header),
					TextCodecs.CODEC.optionalFieldOf("footer").forGetter(CurrentTabList::footer),
					TextCodecs.CODEC.listOf().fieldOf("body").forGetter(CurrentTabList::body),
				).apply(it, ::CurrentTabList)
			}
		}
	}

	private fun getTabListNow(): CurrentTabList {
		// This is a precondition for PlayerListHud.collectEntries to be valid
		MC.networkHandler ?: return CurrentTabList(Optional.empty(), Optional.empty(), emptyList())
		val hud = MC.inGameHud.playerListHud.cast()
		val entries = hud.collectPlayerEntries_firmament()
			.map {
				it.displayName ?: run {
					val team = it.scoreboardTeam
					val name = it.profile.name
					Team.decorateName(team, Text.literal(name))
				}
			}
		return CurrentTabList(
			header = hud.header_firmament.intoOptional(),
			footer = hud.footer_firmament.intoOptional(),
			body = entries,
		)
	}
}
