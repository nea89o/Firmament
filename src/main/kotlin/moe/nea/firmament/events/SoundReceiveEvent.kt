/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.events

import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.Vec3d

data class SoundReceiveEvent(
    val sound: RegistryEntry<SoundEvent>,
    val category: SoundCategory,
    val position: Vec3d,
    val pitch: Float,
    val volume: Float,
    val seed: Long
) : FirmamentEvent.Cancellable() {
    companion object : FirmamentEventBus<SoundReceiveEvent>()
}
