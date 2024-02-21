/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

import moe.nea.firmament.features.FirmamentFeature

data class FeaturesInitializedEvent(val features: List<FirmamentFeature>) : FirmamentEvent() {
    companion object : FirmamentEventBus<FeaturesInitializedEvent>()
}
