/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

data class TickEvent(val tickCount: Int) : FirmamentEvent() {
    companion object : FirmamentEventBus<TickEvent>()
}
