
package moe.nea.firmament.annotations.process

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Origin
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject


class MixinAnnotationProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger
) : SymbolProcessor {
    @AutoService(SymbolProcessorProvider::class)
    class Provider : SymbolProcessorProvider {
        override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
            return MixinAnnotationProcessor(environment.codeGenerator, environment.logger)
        }
    }

    val mixinPackage = "moe.nea.firmament.mixins"
    val refmapName = "Firmament-refmap.json"
    val mixinPlugin = "moe.nea.firmament.init.MixinPlugin"
    val scaffold = """
{
    "required": true,
    "plugin": "moe.nea.firmament.init.MixinPlugin",
    "package": "{mixinPackage}",
    "compatibilityLevel": "JAVA_17",
    "injectors": {
        "defaultRequire": 1
    },
    "refmap": "{refmapName}",
    "client": {mixins}
}
"""
    var rounds = mutableListOf<KSClassDeclaration>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        return resolver.getSymbolsWithAnnotation("org.spongepowered.asm.mixin.Mixin")
            .filter { !processElement(it, resolver) }.toList()
    }

    override fun finish() {
        val output = codeGenerator.createNewFile(
            Dependencies(
                aggregating = true,
                *rounds.map { it.containingFile!! }.toTypedArray()),
            "", "firmament.mixins",
            extensionName = "json")
        val writer = output.writer()
        val gson = Gson()
        val mixinJson = JsonObject()
        mixinJson.addProperty("required", true)
        mixinJson.addProperty("plugin", mixinPlugin)
        mixinJson.addProperty("package", mixinPackage)
        mixinJson.addProperty("compatibilityLevel", "JAVA_21")
        mixinJson.addProperty("refmap", refmapName)
        val mixinArray = JsonArray()
        rounds.map { it.qualifiedName!!.asString().removePrefix("$mixinPackage.") }
            .sorted()
            .forEach(mixinArray::add)
        mixinJson.add("client", mixinArray)
        gson.toJson(mixinJson, writer)
        writer.close()
        rounds
    }

    private fun processElement(decl: KSAnnotated, resolver: Resolver): Boolean {
        if (decl !is KSClassDeclaration) {
            logger.error("@Mixin only allowed on class declarations", decl)
            return true
        }
        decl.qualifiedName ?: logger.error("@Mixin only allowed on classes with a proper name")
        if (decl.origin != Origin.JAVA) logger.error("@Mixin only allowed in java code")
        val packageName = decl.packageName.asString()
        if (packageName != mixinPackage && !packageName.startsWith("$mixinPackage."))
            logger.error("@Mixin outside of mixin package", decl)
        rounds.add(decl)
        return true
    }


}
