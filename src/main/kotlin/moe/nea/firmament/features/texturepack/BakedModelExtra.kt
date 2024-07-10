/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.texturepack

import net.minecraft.client.render.model.BakedModel

interface BakedModelExtra {
    fun getHeadModel_firmament(): BakedModel?
    fun setHeadModel_firmament(headModel: BakedModel?)
}
