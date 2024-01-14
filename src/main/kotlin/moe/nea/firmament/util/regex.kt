/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package moe.nea.firmament.util

import java.util.regex.Matcher
import java.util.regex.Pattern

inline fun <T> String.ifMatches(regex: Regex, block: (MatchResult) -> T): T? =
    regex.matchEntire(this)?.let(block)

inline fun <T> Pattern.useMatch(string: String, block: Matcher.() -> T): T? =
    matcher(string)
        .takeIf(Matcher::matches)
        ?.let(block)
