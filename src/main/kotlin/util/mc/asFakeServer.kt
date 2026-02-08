package moe.nea.firmament.util.mc

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import net.minecraft.commands.CommandSource
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.server.permissions.PermissionSet

fun FabricClientCommandSource.asFakeServer(): CommandSourceStack {
	val source = this
	return CommandSourceStack(
		object : CommandSource {
			override fun sendSystemMessage(message: Component) {
				source.player.displayClientMessage(message, false)
			}

			override fun acceptsSuccess(): Boolean {
				return true
			}

			override fun acceptsFailure(): Boolean {
				return true
			}

			override fun shouldInformAdmins(): Boolean {
				return true
			}
		},
		source.position,
		source.rotation,
		smuggleNull(),
		PermissionSet.NO_PERMISSIONS,
		"FakeServerCommandSource",
		Component.literal("FakeServerCommandSource"),
		smuggleNull(),
		source.player
	)
}

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalContracts::class)
inline fun intimidate(x: Any?) {
	contract {
		returns() implies (x != null)
	}
}

@Suppress("NOTHING_TO_INLINE")
/**
 * This emits a single `aconst_null` instruction, without an [kotlin.jvm.internal.Intrinsics.checkNotNull] call.
 *
 * Nota bene: the only place where this circumvents null checks is at the creation of the value. if the returned value crosses method boundaries into another kotlin method, it will likely cause an NPE even if that nullable value is never used.
 */
inline fun <T : Any> smuggleNull(): T {
	val witness: T? = null
	intimidate(witness)
	return witness
}
