package moe.nea.firmament.features.debug

import net.minecraft.commands.arguments.ResourceKeyArgument
import net.minecraft.core.component.DataComponentType
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.decoration.ArmorStand
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.Tag
import net.minecraft.nbt.NbtOps
import net.minecraft.core.registries.Registries
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.commands.get
import moe.nea.firmament.commands.thenArgument
import moe.nea.firmament.commands.thenExecute
import moe.nea.firmament.commands.thenLiteral
import moe.nea.firmament.events.CommandEvent
import moe.nea.firmament.events.EntityUpdateEvent
import moe.nea.firmament.events.WorldReadyEvent
import moe.nea.firmament.util.ClipboardUtils
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.math.GChainReconciliation
import moe.nea.firmament.util.math.GChainReconciliation.shortenCycle
import moe.nea.firmament.util.mc.NbtPrism
import moe.nea.firmament.util.tr

object AnimatedClothingScanner {

	data class LensOfFashionTheft<T : Any>(
		val prism: NbtPrism,
		val component: DataComponentType<T>,
	) {
		fun observe(itemStack: ItemStack): Collection<Tag> {
			val x = itemStack.get(component) ?: return listOf()
			val nbt = component.codecOrThrow().encodeStart(NbtOps.INSTANCE, x).orThrow
			return prism.access(nbt)
		}
	}

	var lens: LensOfFashionTheft<*>? = null
	var subject: Entity? = null
	var history: MutableList<String> = mutableListOf()
	val metaHistory: MutableList<List<String>> = mutableListOf()

	@OptIn(ExperimentalStdlibApi::class)
	@Subscribe
	fun onUpdate(event: EntityUpdateEvent) {
		val s = subject ?: return
		if (event.entity != s) return
		val l = lens ?: return
		if (event is EntityUpdateEvent.EquipmentUpdate) {
			event.newEquipment.forEach {
				val formatted = (l.observe(it.second)).joinToString()
				history.add(formatted)
				// TODO: add a slot filter
			}
		}
	}

	fun reduceHistory(reducer: (List<String>, List<String>) -> List<String>): List<String> {
		return metaHistory.fold(history, reducer).shortenCycle()
	}

	@Subscribe
	fun onSubCommand(event: CommandEvent.SubCommand) {
		event.subcommand(DeveloperFeatures.DEVELOPER_SUBCOMMAND) {
			thenLiteral("stealthisfit") {
				thenLiteral("clear") {
					thenExecute {
						subject = null
						metaHistory.clear()
						history.clear()
						MC.sendChat(tr("firmament.fitstealer.clear", "Cleared fit stealing history"))
					}
				}
				thenLiteral("copy") {
					thenExecute {
						val history = reduceHistory { a, b -> a + b }
						copyHistory(history)
						MC.sendChat(tr("firmament.fitstealer.copied", "Copied the history"))
					}
					thenLiteral("deduplicated") {
						thenExecute {
							val history = reduceHistory { a, b ->
								(a.toMutableSet() + b).toList()
							}
							copyHistory(history)
							MC.sendChat(
								tr(
									"firmament.fitstealer.copied.deduplicated",
									"Copied the deduplicated history"
								)
							)
						}
					}
					thenLiteral("merged") {
						thenExecute {
							val history = reduceHistory(GChainReconciliation::reconcileCycles)
							copyHistory(history)
							MC.sendChat(tr("firmament.fitstealer.copied.merged", "Copied the merged history"))
						}
					}
				}
				thenLiteral("target") {
					thenLiteral("self") {
						thenExecute {
							toggleObserve(MC.player!!)
						}
					}
					thenLiteral("pet") {
						thenExecute {
							source.sendFeedback(
								tr(
									"firmament.fitstealer.stealingpet",
									"Observing nearest marker armourstand"
								)
							)
							val p = MC.player!!
							val nearestPet = p.level.getEntitiesOfClass(
								ArmorStand::class.java,
								p.boundingBox.inflate(10.0),
								{ it.isMarker })
								.minBy { it.distanceToSqr(p) }
							toggleObserve(nearestPet)
						}
					}
					thenExecute {
						val ent = MC.instance.crosshairPickEntity
						if (ent == null) {
							source.sendFeedback(
								tr(
									"firmament.fitstealer.notargetundercursor",
									"No entity under cursor"
								)
							)
						} else {
							toggleObserve(ent)
						}
					}
				}
				thenLiteral("path") {
					thenArgument(
						"component",
						ResourceKeyArgument.key(Registries.DATA_COMPONENT_TYPE)
					) { component ->
						thenArgument("path", NbtPrism.Argument) { path ->
							thenExecute {
								lens = LensOfFashionTheft(
									get(path),
									MC.unsafeGetRegistryEntry(get(component))!!,
								)
								source.sendFeedback(
									tr(
										"firmament.fitstealer.lensset",
										"Analyzing path ${get(path)} for component ${get(component).identifier()}"
									)
								)
							}
						}
					}
				}
			}
		}
	}

	private fun copyHistory(toCopy: List<String>) {
		ClipboardUtils.setTextContent(toCopy.joinToString("\n"))
	}

	@Subscribe
	fun onWorldSwap(event: WorldReadyEvent) {
		subject = null
		if (history.isNotEmpty()) {
			metaHistory.add(history)
			history = mutableListOf()
		}
	}

	private fun toggleObserve(entity: Entity?) {
		subject = if (subject == null) entity else null
		if (subject == null) {
			metaHistory.add(history)
			history = mutableListOf()
		}
		MC.sendChat(
			subject?.let {
				tr(
					"firmament.fitstealer.targeted",
					"Observing the equipment of ${it.name}."
				)
			} ?: tr("firmament.fitstealer.targetlost", "No longer logging equipment."),
		)
	}
}
