package moe.nea.firmament.util

import java.util.Optional

fun <T : Any> T?.intoOptional(): Optional<T> = Optional.ofNullable(this)
