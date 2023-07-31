/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util

import java.math.BigInteger
import java.util.UUID

fun parseDashlessUUID(dashlessUuid: String): UUID {
    val most = BigInteger(dashlessUuid.substring(0, 16), 16)
    val least = BigInteger(dashlessUuid.substring(16, 32), 16)
    return UUID(most.toLong(), least.toLong())
}
