
fun execString(vararg args: String): String {
    val pb = ProcessBuilder(*args)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .start()
    pb.waitFor()
    return pb.inputStream.readAllBytes().decodeToString().trim()
}

private val tag = "([0-9.]+)\\.0".toRegex()
private val tagOffset = "([0-9.]+)\\.0-([0-9]+)..+".toRegex()

inline fun <T> Regex.useMatcher(string: String, block: (MatchResult) -> T): T? {
    return matchEntire(string)?.let(block)
}

fun getGitTagInfo(): String {
    val str = execString("git", "describe", "--tags", "HEAD")
    tag.useMatcher(str) {
        return it.groupValues[0]
    }
    tagOffset.useMatcher(str) {
        return it.groupValues[1] + "." + it.groupValues[2]
    }
    return "nogitversion"
}
