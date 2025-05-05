package moe.nea.firmament.features.debug

import net.minecraft.command.argument.RegistryKeyArgumentType
import net.minecraft.component.ComponentType
import net.minecraft.component.DataComponentTypes
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtOps
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier
import moe.nea.firmament.annotations.Subscribe
import moe.nea.firmament.commands.get
import moe.nea.firmament.commands.thenArgument
import moe.nea.firmament.commands.thenExecute
import moe.nea.firmament.commands.thenLiteral
import moe.nea.firmament.events.CommandEvent
import moe.nea.firmament.events.EntityUpdateEvent
import moe.nea.firmament.util.ClipboardUtils
import moe.nea.firmament.util.MC
import moe.nea.firmament.util.mc.NbtPrism
import moe.nea.firmament.util.skyBlockId
import moe.nea.firmament.util.tr

object AnimatedClothingScanner {

	data class SubjectOfFashionTheft<T>(
		val observedEntity: Entity,
		val prism: NbtPrism,
		val component: ComponentType<T>,
	) {
		fun observe(itemStack: ItemStack): Collection<NbtElement> {
			val x = itemStack.get(component) ?: return listOf()
			val nbt = component.codecOrThrow.encodeStart(NbtOps.INSTANCE, x).orThrow
			return prism.access(nbt)
		}
	}

	var subject: SubjectOfFashionTheft<*>? = null

	@OptIn(ExperimentalStdlibApi::class)
	@Subscribe
	fun onUpdate(event: EntityUpdateEvent) {
		val s = subject ?: return
		if (event.entity != s.observedEntity) return
		if (event is EntityUpdateEvent.EquipmentUpdate) {
			val lines = mutableListOf<String>()
			event.newEquipment.forEach {
				val formatted = (s.observe(it.second)).joinToString()
				lines.add(formatted)
				MC.sendChat(
					tr(
						"firmament.fitstealer.update",
						"[FIT CHECK][${MC.currentTick}] ${it.first.asString()} => $formatted"
					)
				)
			}
			if (lines.isNotEmpty()) {
				val contents = ClipboardUtils.getTextContents()
				if (contents.startsWith(EXPORT_WATERMARK))
					ClipboardUtils.setTextContent(
						contents + "\n" + lines.joinToString("\n")
					)
			}
		}
	}

	val EXPORT_WATERMARK = "[CLOTHES EXPORT]"

	@Subscribe
	fun onSubCommand(event: CommandEvent.SubCommand) {
		event.subcommand("dev") {
			thenLiteral("stealthisfit") {
				thenArgument(
					"component",
					RegistryKeyArgumentType.registryKey(RegistryKeys.DATA_COMPONENT_TYPE)
				) { component ->
					thenArgument("path", NbtPrism.Argument) { path ->
						thenExecute {
							subject =
								if (subject == null) run {
									val entity = MC.instance.targetedEntity ?: return@run null
									val clipboard = ClipboardUtils.getTextContents()
									if (!clipboard.startsWith(EXPORT_WATERMARK)) {
										ClipboardUtils.setTextContent(EXPORT_WATERMARK)
									} else {
										ClipboardUtils.setTextContent("$clipboard\n\n[NEW SCANNER]")
									}
									SubjectOfFashionTheft(
										entity,
										get(path),
										MC.unsafeGetRegistryEntry(get(component))!!,
									)
								} else null

							MC.sendChat(
								subject?.let {
									tr(
										"firmament.fitstealer.targeted",
										"Observing the equipment of ${it.observedEntity.name}."
									)
								} ?: tr("firmament.fitstealer.targetlost", "No longer logging equipment."),
							)
						}
					}
				}
			}
		}
	}
}
