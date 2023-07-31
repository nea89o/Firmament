/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util

inline fun <T> String.ifMatches(regex: Regex, block: (MatchResult) -> T): T? =
    regex.matchEntire(this)?.let(block)
