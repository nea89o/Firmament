package moe.nea.firmament.util

object TestUtil {
	val isInTest = Thread.currentThread().stackTrace.any { it.className.startsWith("org.junit.") }
}
