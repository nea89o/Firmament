import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.io.File
import java.util.zip.ZipInputStream
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream

abstract class InnerJarsUnpacker : DefaultTask() {
    @get:InputFiles
    abstract val inputJars: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    private fun getFabricModJson(inputFile: File): JsonObject {
        inputFile.inputStream().use {
            val zis = ZipInputStream(it)
            while (true) {
                val entry = zis.nextEntry ?: error("Failed to find fabric.mod.json")
                if (entry.name == "fabric.mod.json") {
                    return Gson().fromJson(zis.reader(), JsonObject::class.java)
                }
            }
        }
    }

    @TaskAction
    fun unpack() {
        inputJars.forEach { inputFile ->
            val fabricModObject = getFabricModJson(inputFile)
            val jars = fabricModObject["jars"] as? JsonArray ?: error("No jars to unpack in $inputFile")
            val jarPaths = jars.map {
                ((it as? JsonObject)?.get("file") as? JsonPrimitive)?.asString
                    ?: error("Invalid Jar $it in $inputFile")
            }
            extractJars(inputFile, jarPaths)
        }
    }

    private fun extractJars(inputFile: File, jarPaths: List<String>) {
        val outputFile = outputDir.get().asFile.toPath()
        val jarPathSet = jarPaths.toMutableSet()
        inputFile.inputStream().use {
            val zis = ZipInputStream(it)
            while (true) {
                val entry = zis.nextEntry ?: break
                if (jarPathSet.remove(entry.name)) {
                    val resolvedPath = outputFile.resolve(entry.name)
                    resolvedPath.parent.createDirectories()
                    resolvedPath.outputStream().use { os ->
                        zis.copyTo(os)
                    }
                }
            }
        }
        if (jarPathSet.isNotEmpty()) {
            error("Could not extract all jars: $jarPathSet")
        }
    }
}
