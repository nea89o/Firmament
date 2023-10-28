/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util


fun <T> errorBoundary(block: () -> T): T? {
    // TODO: implement a proper error boundary here to avoid crashing minecraft code
    return block()
}

