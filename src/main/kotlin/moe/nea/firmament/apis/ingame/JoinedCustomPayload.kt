/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.apis.ingame

import net.minecraft.network.packet.CustomPayload

/**
 * A class to smuggle two parsed instances of the same custom payload packet.
 */
class JoinedCustomPayload(
    val original: CustomPayload,
    val smuggled: FirmamentCustomPayload
) : CustomPayload {
    companion object {
        val joinedId = CustomPayload.id<JoinedCustomPayload>("firmament:joined")
    }

    override fun getId(): CustomPayload.Id<out JoinedCustomPayload> {
        return joinedId
    }
}
