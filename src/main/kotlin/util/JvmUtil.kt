package moe.nea.firmament.util

import com.sun.tools.attach.VirtualMachine
import java.lang.management.ManagementFactory
import java.nio.file.Path
import kotlin.io.path.absolutePathString

object JvmUtil {
	fun guessJVMPid(): String {
		val name = ManagementFactory.getRuntimeMXBean().name
		val pid = name.substringBefore('@')
		ErrorUtil.softCheck("Not a valid PID: $pid", pid.toIntOrNull() != null)
		return pid
	}

	fun getVM(): VirtualMachine {
		return VirtualMachine.attach(guessJVMPid())
	}

	fun useVM(block: (VirtualMachine) -> Unit) {
		val vm = getVM()
		block(vm)
		vm.detach()
	}

	fun loadAgent(jarPath: Path, options: String? = null) {
		useVM {
			it.loadAgent(jarPath.absolutePathString(), options)
		}
	}

}
