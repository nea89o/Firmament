/*
 * Firmament is a Hypixel Skyblock mod for modern Minecraft versions
 * Copyright (C) 2023 Linnea Gräf
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package moe.nea.firmament.events

import moe.nea.firmament.util.Locraw

/**
 * This event gets published whenever `/locraw` is queried and HyPixel returns a location different to the old one.
 *
 * **N.B.:** This event may get fired multiple times while on the server (for example, first to null, then to the
 * correct location).
 */
data class SkyblockServerUpdateEvent(val oldLocraw: Locraw?, val newLocraw: Locraw?) : FirmamentEvent() {
    companion object : FirmamentEventBus<SkyblockServerUpdateEvent>()
}
