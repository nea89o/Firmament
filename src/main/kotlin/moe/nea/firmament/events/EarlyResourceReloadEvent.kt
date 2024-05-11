/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

import java.util.concurrent.Executor
import net.minecraft.resource.ResourceManager

data class EarlyResourceReloadEvent(val resourceManager: ResourceManager, val preparationExecutor: Executor) :
    FirmamentEvent() {
    companion object : FirmamentEventBus<EarlyResourceReloadEvent>()
}
