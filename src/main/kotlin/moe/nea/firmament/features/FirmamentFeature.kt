/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features

import moe.nea.firmament.events.subscription.SubscriptionOwner
import moe.nea.firmament.gui.config.ManagedConfig

interface FirmamentFeature : SubscriptionOwner {
    val identifier: String
    val defaultEnabled: Boolean
        get() = true
    var isEnabled: Boolean
        get() = FeatureManager.isEnabled(identifier) ?: defaultEnabled
        set(value) {
            FeatureManager.setEnabled(identifier, value)
        }
    override val delegateFeature: FirmamentFeature
        get() = this
    val config: ManagedConfig? get() = null
    fun onLoad() {}

}
