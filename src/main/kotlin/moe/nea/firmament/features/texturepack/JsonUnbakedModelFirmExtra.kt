/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.texturepack

import net.minecraft.util.Identifier

interface JsonUnbakedModelFirmExtra {

    fun setHeadModel_firmament(identifier: Identifier?)
    fun getHeadModel_firmament(): Identifier?
}
