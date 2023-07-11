package moe.nea.firmament.util

inline fun <T> String.ifMatches(regex: Regex, block: (MatchResult) -> T): T? =
    regex.matchEntire(this)?.let(block)
