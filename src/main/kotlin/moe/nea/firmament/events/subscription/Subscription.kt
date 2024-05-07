/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events.subscription

import moe.nea.firmament.events.FirmamentEvent
import moe.nea.firmament.events.FirmamentEventBus

interface SubscriptionOwner

data class Subscription<T : FirmamentEvent>(
    val owner: SubscriptionOwner,
    val invoke: (T) -> Unit,
    val eventBus: FirmamentEventBus<T>,
)
