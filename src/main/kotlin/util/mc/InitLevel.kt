package moe.nea.firmament.util.mc

enum class InitLevel {
	STARTING,
	MC_INIT,
	RENDER_INIT,
	RENDER,
	MAIN_MENU,
	;

	companion object {
		var initLevel = InitLevel.STARTING
			private set

		@JvmStatic
		fun isAtLeast(wantedLevel: InitLevel): Boolean = initLevel >= wantedLevel

		@JvmStatic
		fun bump(nextLevel: InitLevel) {
			if (nextLevel.ordinal != initLevel.ordinal + 1)
				error("Cannot bump initLevel $nextLevel from $initLevel")
			initLevel = nextLevel
		}
	}
}
