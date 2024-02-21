/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.features.texturepack

interface BakedOverrideData {
    fun getFirmamentOverrides(): Array<FirmamentModelPredicate>?
    fun setFirmamentOverrides(overrides: Array<FirmamentModelPredicate>?)

}
