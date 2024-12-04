fun execString(vararg args: String): String {
	val pb = ProcessBuilder(*args)
		.redirectOutput(ProcessBuilder.Redirect.PIPE)
		.start()
	pb.waitFor()
	return pb.inputStream.readAllBytes().decodeToString().trim()
}

private val tag = "([0-9.]+)(?:\\+[^-]*)?".toRegex()
private val tagOffset = "([0-9.]+)(?:\\+.*)?-([0-9]+)-(.+)".toRegex()

inline fun <T> Regex.useMatcher(string: String, block: (MatchResult) -> T): T? {
	return matchEntire(string)?.let(block)
}

fun getGitTagInfo(mcVersion: String): String {
	val str = execString("git", "describe", "--tags", "HEAD")
	tag.useMatcher(str) {
		return it.groupValues[1] + "+mc$mcVersion"
	}
	tagOffset.useMatcher(str) {
		return it.groupValues[1] + "-dev+mc$mcVersion+" + it.groupValues[3]
	}
	return "nogitversion+mc$mcVersion"
}
