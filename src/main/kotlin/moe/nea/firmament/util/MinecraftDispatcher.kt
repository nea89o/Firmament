/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util

import kotlinx.coroutines.asCoroutineDispatcher
import net.minecraft.client.MinecraftClient

val MinecraftDispatcher by lazy { MinecraftClient.getInstance().asCoroutineDispatcher() }
