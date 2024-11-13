import java.net.URI
import java.util.zip.ZipInputStream
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class RepoDownload : DefaultTask() {
	@get:Input
	abstract val hash: Property<String>

	@get:OutputDirectory
	abstract val outputDirectory: DirectoryProperty

	init {
		outputDirectory.convention(project.layout.buildDirectory.dir("extracted-test-repo"))
	}

	@TaskAction
	fun performDownload() {
		val outputDir = outputDirectory.asFile.get().absoluteFile
		outputDir.mkdirs()
		URI("https://github.com/notEnoughUpdates/notEnoughUpdates-rEPO/archive/${hash.get()}.zip").toURL().openStream()
			.let(::ZipInputStream)
			.use { zipInput ->
				while (true) {
					val entry = zipInput.nextEntry ?: break
					val destination = outputDir.resolve(
						entry.name.substringAfter('/')).absoluteFile
					require(outputDir in generateSequence(destination) { it.parentFile })
					if (entry.isDirectory) continue
					destination.parentFile.mkdirs()
					destination.outputStream().use { output ->
						zipInput.copyTo(output)
					}
				}
			}
	}
}
