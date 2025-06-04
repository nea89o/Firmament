package moe.nea.firmament.util

object TestUtil {
	inline fun <T> unlessTesting(block: () -> T): T? = if (isInTest) null else block()
	@JvmField
	val isInTest =
		Thread.currentThread().stackTrace.any {
			it.className.startsWith("org.junit.") || it.className.startsWith("io.kotest.")
		}
}
