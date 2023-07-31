/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util

import kotlinx.serialization.Serializable

@Serializable
data class Locraw(val server: String, val gametype: String? = null, val mode: String? = null, val map: String? = null) {
    val skyblockLocation = if (gametype == "SKYBLOCK") mode else null
}
