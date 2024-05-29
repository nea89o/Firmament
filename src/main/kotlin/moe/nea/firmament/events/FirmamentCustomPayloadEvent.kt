/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

import moe.nea.firmament.apis.ingame.FirmamentCustomPayload

data class FirmamentCustomPayloadEvent(
    val payload: FirmamentCustomPayload
) : FirmamentEvent() {
    companion object : FirmamentEventBus<FirmamentCustomPayloadEvent>()
}
