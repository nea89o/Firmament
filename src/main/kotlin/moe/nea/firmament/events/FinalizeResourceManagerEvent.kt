/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

import net.minecraft.resource.ReloadableResourceManagerImpl

data class FinalizeResourceManagerEvent(
    val resourceManager: ReloadableResourceManagerImpl,
) : FirmamentEvent() {
    companion object : FirmamentEventBus<FinalizeResourceManagerEvent>()
}
