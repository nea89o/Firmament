/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util

import moe.nea.firmament.repo.HypixelStaticData

enum class BazaarPriceStrategy {
    BUY_ORDER,
    SELL_ORDER,
    NPC_SELL;

    fun getSellPrice(skyblockId: SkyblockId): Double {
        val bazaarEntry = HypixelStaticData.bazaarData[skyblockId] ?: return 0.0
        return when (this) {
            BUY_ORDER -> bazaarEntry.quickStatus.sellPrice
            SELL_ORDER -> bazaarEntry.quickStatus.buyPrice
            NPC_SELL -> TODO()
        }
    }
}
