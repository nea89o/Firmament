package moe.nea.firmament.util

import java.time.LocalDateTime
import java.time.Month

object AprilFoolsUtil {
	val isAprilFoolsDay = LocalDateTime.now().let {
		it.dayOfMonth == 1 && it.month == Month.APRIL
	}
}
