package moe.nea.firmament.util.skyblock

import java.util.UUID
import net.hypixel.modapi.HypixelModAPI
import net.hypixel.modapi.packet.impl.clientbound.ClientboundPartyInfoPacket
import net.hypixel.modapi.packet.impl.clientbound.ClientboundPartyInfoPacket.PartyRole
import net.hypixel.modapi.packet.impl.serverbound.ServerboundPartyInfoPacket
import org.intellij.lang.annotations.Language
import kotlinx.coroutines.launch
import net.minecraft.network.chat.Component
import moe.nea.firmament.Firmament
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.apis.Routes
import moe.nea.firmament.commands.thenExecute
import moe.nea.firmament.commands.thenLiteral
import moe.nea.firmament.events.CommandEvent
import moe.nea.firmament.events.ProcessChatEvent
import moe.nea.firmament.events.WorldReadyEvent
import moe.nea.firmament.features.debug.DeveloperFeatures
import moe.nea.firmament.util.ErrorUtil
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.SBData
import moe.nea.firmament.util.bold
import moe.nea.firmament.util.boolColour
import moe.nea.firmament.util.grey
import moe.nea.firmament.util.tr
import moe.nea.firmament.util.useMatch

object PartyUtil {
	object Internal {
		val hma = HypixelModAPI.getInstance()

		val handler = hma.createHandler(ClientboundPartyInfoPacket::class.java) { clientboundPartyInfoPacket ->
			Firmament.coroutineScope.launch {
				party = Party(clientboundPartyInfoPacket.memberMap.values.map {
					PartyMember.fromUuid(it.uuid, it.role)
				})
			}
		}

		fun sendSyncPacket() {
			if (!SBData.isOnHypixel) return
			hma.sendPacket(ServerboundPartyInfoPacket())
		}

		@Subscribe
		fun onDevCommand(event: CommandEvent.SubCommand) {
			event.subcommand(DeveloperFeatures.DEVELOPER_SUBCOMMAND) {
				thenLiteral("party") {
					thenLiteral("refresh") {
						thenExecute {
							sendSyncPacket()
							source.sendFeedback(tr("firmament.dev.partyinfo.refresh", "Refreshing party info"))
						}
					}
					thenExecute {
						val p = party
						val text = Component.empty()
						text.append(
							tr("firmament.dev.partyinfo", "Party Info: ")
								.boolColour(p != null)
						)
						if (p == null) {
							text.append(tr("firmament.dev.partyinfo.empty", "Empty Party").grey())
						} else {
							text.append(tr("firmament.dev.partyinfo.count", "${p.members.size} members").grey())
							p.members.forEach {
								text.append("\n")
									.append(Component.literal(" - ${it.name}"))
									.append(" (")
									.append(
										when (it.role) {
											PartyRole.LEADER -> tr("firmament.dev.partyinfo.leader", "Leader").bold()
											PartyRole.MOD -> tr("firmament.dev.partyinfo.mod", "Moderator")
											PartyRole.MEMBER -> tr("firmament.dev.partyinfo.member", "Member")
										}
									)
									.append(")")
							}
						}
						source.sendFeedback(text)
					}
				}
			}
		}

		object Regexes {
			@Language("RegExp")
			val NAME = "(\\[[^\\]]+\\] )?(?<name>[a-zA-Z0-9_]{2,16})"
			val NAME_SECONDARY = NAME.replace("name", "name2")
			val joinSelf = "You have joined $NAME's? party!".toPattern()
			val joinOther = "$NAME joined the party\\.".toPattern()
			val leaveSelf = "You left the party\\.".toPattern()
			val disbandedEmpty =
				"The party was disbanded because all invites expired and the party was empty\\.".toPattern()
			val leaveOther = "$NAME has left the party\\.".toPattern()
			val kickedOther = "$NAME has been removed from the party\\.".toPattern()
			val kickedOtherOffline = "Kicked $NAME because they were offline\\.".toPattern()
			val disconnectedOther = "$NAME was removed from your party because they disconnected\\.".toPattern()
			val transferLeave = "The party was transferred to $NAME because $NAME_SECONDARY left\\.?".toPattern()
			val transferVoluntary = "The party was transferred to $NAME by $NAME_SECONDARY\\.?".toPattern()
			val disbanded = "$NAME has disbanded the party!".toPattern()
			val kickedSelf = "You have been kicked from the party by $NAME ?\\.?".toPattern()
			val partyFinderJoin = "Party Finder > $NAME joined the .* group!.*".toPattern()
		}

		fun modifyParty(
			allowEmpty: Boolean = false,
			modifier: (MutableList<PartyMember>) -> Unit
		) {
			val oldList = party?.members ?: emptyList()
			if (oldList.isEmpty() && !allowEmpty) return
			party = Party(oldList.toMutableList().also(modifier))
		}

		fun MutableList<PartyMember>.modifyMember(name: String, mod: (PartyMember) -> PartyMember) {
			val idx = indexOfFirst { it.name == name }
			val member = if (idx < 0) {
				PartyMember(name, PartyRole.MEMBER)
			} else {
				removeAt(idx)
			}
			add(mod(member))
		}

		fun addMemberToParty(name: String) {
			modifyParty(true) {
				if (it.isEmpty())
					it.add(PartyMember(MC.playerName, PartyRole.LEADER))
				it.add(PartyMember(name, PartyRole.MEMBER))
			}
		}

		@Subscribe
		fun onJoinServer(event: WorldReadyEvent) { // This event isn't perfect... Hypixel isn't ready yet when we join the server. We should probably just listen to the mod api hello packet and go from there, but this works (since you join and leave servers quite often).
			if (party == null)
				sendSyncPacket()
		}

		@Subscribe
		fun onPartyRelatedMessage(event: ProcessChatEvent) {
			Regexes.joinSelf.useMatch(event.unformattedString) {
				sendSyncPacket()
			}
			Regexes.joinOther.useMatch(event.unformattedString) {
				addMemberToParty(group("name"))
			}
			Regexes.leaveOther.useMatch(event.unformattedString) {
				modifyParty { it.removeIf { it.name == group("name") } }
			}
			Regexes.leaveSelf.useMatch(event.unformattedString) {
				modifyParty { it.clear() }
			}
			Regexes.disbandedEmpty.useMatch(event.unformattedString) {
				modifyParty { it.clear() }
			}
			Regexes.kickedOther.useMatch(event.unformattedString) {
				modifyParty { it.removeIf { it.name == group("name") } }
			}
			Regexes.kickedOtherOffline.useMatch(event.unformattedString) {
				modifyParty { it.removeIf { it.name == group("name") } }
			}
			Regexes.disconnectedOther.useMatch(event.unformattedString) {
				modifyParty { it.removeIf { it.name == group("name") } }
			}
			Regexes.transferLeave.useMatch(event.unformattedString) {
				modifyParty {
					it.modifyMember(group("name")) { it.copy(role = PartyRole.LEADER) }
					it.removeIf { it.name == group("name2") }
				}
			}
			Regexes.transferVoluntary.useMatch(event.unformattedString) {
				modifyParty {
					it.modifyMember(group("name")) { it.copy(role = PartyRole.LEADER) }
					it.modifyMember(group("name2")) { it.copy(role = PartyRole.MOD) }
				}
			}
			Regexes.disbanded.useMatch(event.unformattedString) {
				modifyParty { it.clear() }
			}
			Regexes.kickedSelf.useMatch(event.unformattedString) {
				modifyParty { it.clear() }
			}
			Regexes.partyFinderJoin.useMatch(event.unformattedString) {
				addMemberToParty(group("name"))
			}
		}
	}

	data class Party(
		val members: List<PartyMember>
	)

	data class PartyMember(
		val name: String,
		val role: PartyRole
	) {
		companion object {
			suspend fun fromUuid(uuid: UUID, role: PartyRole = PartyRole.MEMBER): PartyMember {
				return PartyMember(
					ErrorUtil.notNullOr(
						Routes.getPlayerNameForUUID(uuid),
						"Could not find username for player $uuid"
					) { "Ghost" },
					role
				)
			}
		}
	}

	var party: Party? = null
}
