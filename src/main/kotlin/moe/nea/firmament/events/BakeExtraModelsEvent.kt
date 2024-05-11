/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

import java.util.function.Consumer
import net.minecraft.client.util.ModelIdentifier

class BakeExtraModelsEvent(
    private val addModel: Consumer<ModelIdentifier>,
) : FirmamentEvent() {

    fun addModel(modelIdentifier: ModelIdentifier) {
        this.addModel.accept(modelIdentifier)
    }

    companion object : FirmamentEventBus<BakeExtraModelsEvent>()
}
