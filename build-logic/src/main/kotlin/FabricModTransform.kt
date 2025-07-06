import com.github.jengelman.gradle.plugins.shadow.transformers.ResourceTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.TransformerContext
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.io.Serializable
import net.fabricmc.accesswidener.AccessWidenerReader
import net.fabricmc.accesswidener.AccessWidenerWriter
import org.apache.tools.zip.ZipEntry
import org.apache.tools.zip.ZipOutputStream
import org.gradle.api.file.FileTreeElement
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

open class FabricModTransform : ResourceTransformer {

	enum class AccessWidenerInclusion : Serializable {
		ALL,
		NONE,
	}

	@get:Input
	var mergeAccessWideners: AccessWidenerInclusion = AccessWidenerInclusion.ALL

	@get:Internal
	internal var mergedFmj: JsonObject? = null

	@get:Internal
	internal val foundAccessWideners = AccessWidenerWriter()

	@get:Internal
	internal var foundAnyAccessWidener = false

	override fun canTransformResource(element: FileTreeElement): Boolean {
		if (mergeAccessWideners == AccessWidenerInclusion.ALL && element.name.endsWith(".accesswidener"))
			return true
		return element.path == "fabric.mod.json"
	}

	override fun transform(context: TransformerContext) {
		if (context.path.endsWith(".accesswidener")) {
			foundAnyAccessWidener = true
			// TODO: allow filtering for only those mentioned in a fabric.mod.json, potentially
			context.inputStream.use { stream ->
				AccessWidenerReader(foundAccessWideners).read(stream.bufferedReader())
			}
			return
		}
		// TODO: mixins.json relocations
		val fmj = context.inputStream.use { stream ->
			Gson().fromJson(stream.bufferedReader(), JsonObject::class.java)
		}
		val mergedFmj = this.mergedFmj
		println("${fmj["id"]} is first? ${mergedFmj == null}")
		if (mergedFmj == null) {
			this.mergedFmj = fmj
		} else {
			// TODO: merge stuff
		}
	}

	override fun hasTransformedResource(): Boolean {
		return mergedFmj != null
	}

	override fun modifyOutputStream(os: ZipOutputStream, preserveFileTimestamps: Boolean) {
		val mergedFmj = mergedFmj!!
		if (foundAnyAccessWidener) {
			val awFile = mergedFmj["accessWidener"]
			require(awFile is JsonPrimitive && awFile.isString)
			os.putNextEntry(ZipEntry(awFile.asString))
			os.write(foundAccessWideners.write())
			os.closeEntry()
		}
		os.putNextEntry(ZipEntry("fabric.mod.json"))
		os.write(mergedFmj.toString().toByteArray())
		os.closeEntry()
	}
}
