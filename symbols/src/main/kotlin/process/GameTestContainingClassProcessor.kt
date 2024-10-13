package moe.nea.firmament.annotations.process

import com.google.auto.service.AutoService
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.TreeSet

class GameTestContainingClassProcessor(
	val logger: KSPLogger,
	val codeGenerator: CodeGenerator,
	val sourceSetName: String,
) : SymbolProcessor {


	@AutoService(SymbolProcessorProvider::class)
	class Provider : SymbolProcessorProvider {
		override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
			return GameTestContainingClassProcessor(
				environment.logger,
				environment.codeGenerator,
				environment.options["firmament.sourceset"] ?: "main")
		}
	}

	val allClasses: MutableSet<String> = TreeSet()
	val allSources = mutableSetOf<KSFile>()

	override fun process(resolver: Resolver): List<KSAnnotated> {
		val annotated = resolver.getSymbolsWithAnnotation("net.minecraft.test.GameTest").toList()
		annotated.forEach {
			val containingClass = it.parent as KSClassDeclaration
			allClasses.add(containingClass.qualifiedName!!.asString())
			allSources.add(it.containingFile!!)
		}
		return emptyList()
	}

	fun createJson(): JsonObject {
		return JsonObject().apply {
			addProperty("schemaVersion", 1)
			addProperty("id", "firmament-gametest")
			addProperty("name", "Firmament Gametest")
			addProperty("version", "1.0.0")
			addProperty("environment", "*")
			add("entrypoints", JsonObject().apply {
				add("fabric-gametest", JsonArray().apply {
					allClasses.forEach {
						add(it)
					}
				})
			})
		}
	}

	override fun finish() {
		if (allClasses.isEmpty()) return
		val stream = codeGenerator.createNewFile(Dependencies(aggregating = true, *allSources.toTypedArray()),
		                                         "",
		                                         "fabric.mod",
		                                         "json")
		val output = OutputStreamWriter(stream, StandardCharsets.UTF_8)
		Gson().toJson(createJson(), output)
		output.close()
	}

}
