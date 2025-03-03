/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: CC0-1.0
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

import com.google.common.hash.Hashing
import com.google.devtools.ksp.gradle.KspTaskJvm
import com.google.gson.Gson
import com.google.gson.JsonObject
import moe.nea.licenseextractificator.LicenseDiscoveryTask
import moe.nea.mcautotranslations.gradle.CollectTranslations
import net.fabricmc.loom.LoomGradleExtension
import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.nio.charset.StandardCharsets
import java.util.Base64

plugins {
	java
	`maven-publish`
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.plugin.serialization)
	alias(libs.plugins.kotlin.plugin.powerassert)
	alias(libs.plugins.kotlin.plugin.ksp)
	//	alias(libs.plugins.loom)
	// TODO: use arch loom once they update to 1.8
	id("fabric-loom") version "1.9.2"
	alias(libs.plugins.shadow)
	id("moe.nea.licenseextractificator")
	id("moe.nea.mc-auto-translations") version "0.1.0"
}

version = getGitTagInfo(libs.versions.minecraft.get())
group = rootProject.property("maven_group").toString()

java {
	withSourcesJar()
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}
loom {
	mixin.useLegacyMixinAp.set(false)
}

tasks.withType(KotlinCompile::class) {
	compilerOptions {
		jvmTarget.set(JvmTarget.JVM_21)
	}
}

allprojects {
	repositories {
		mavenCentral()
		maven("https://maven.terraformersmc.com/releases/")
		maven("https://maven.shedaniel.me")
		maven("https://maven.fabricmc.net")
		maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
		maven("https://api.modrinth.com/maven") {
			content {
				includeGroup("maven.modrinth")
			}
		}
		maven("https://repo.sleeping.town") {
			content {
				includeGroup("com.unascribed")
			}
		}
		ivy("https://github.com/HotswapProjects/HotswapAgent/releases/download") {
			patternLayout {
				artifact("[revision]/[artifact]-[revision].[ext]")
			}
			content {
				includeGroup("virtual.github.hotswapagent")
			}
			metadataSources {
				artifact()
			}
		}
		maven("https://server.bbkr.space/artifactory/libs-release")
		maven("https://repo.nea.moe/releases")
		maven("https://maven.notenoughupdates.org/releases")
		maven("https://repo.nea.moe/mirror")
		maven("https://jitpack.io/") {
			content {
				includeGroupByRegex("(com|io)\\.github\\..+")
				excludeModule("io.github.cottonmc", "LibGui")
			}
		}
		maven("https://repo.hypixel.net/repository/Hypixel/")
		maven("https://maven.azureaaron.net/snapshots")
		maven("https://maven.azureaaron.net/releases")
		maven("https://www.cursemaven.com")
		maven("https://maven.isxander.dev/releases") {
			name = "Xander Maven"
		}
		mavenLocal()
	}
}
kotlin {
	sourceSets.all {
		languageSettings {
			enableLanguageFeature("BreakContinueInInlineLambdas")
		}
	}
}
fun String.capitalizeN() = replaceFirstChar { it.uppercaseChar() }
// Usually a normal sync takes care of this, but in CI everything needs to run in one shot, so we need to improvise.
val unpackAllJars by tasks.registering
fun innerJarsOf(name: String, dependency: Dependency): Provider<FileTree> {
	val task = tasks.create("unpackInnerJarsFor${name.capitalizeN()}", InnerJarsUnpacker::class) {
		this.inputJars.setFrom(files(configurations.detachedConfiguration(dependency)))
		this.outputDir.set(layout.buildDirectory.dir("unpackedJars/$name").also {
			it.get().asFile.mkdirs()
		})
	}
	unpackAllJars { dependsOn(task) }
	return project.provider {
		project.files(task).asFileTree
	}
}

val collectTranslations by tasks.registering(CollectTranslations::class) {
	this.baseTranslations.from(file("translations/en_us.json"))
	this.classes.from(sourceSets.main.get().kotlin.classesDirectory)
}

val compatSourceSets: MutableSet<SourceSet> = mutableSetOf()
fun createIsolatedSourceSet(name: String, path: String = "compat/$name", isEnabled: Boolean = true): SourceSet {
	val ss = sourceSets.create(name) {
		this.java.setSrcDirs(listOf(layout.projectDirectory.dir("src/$path/java")))
		this.kotlin.setSrcDirs(listOf(layout.projectDirectory.dir("src/$path/java")))
	}
	val mainSS = sourceSets.main.get()
	val upperName = ss.name.capitalizeN()
	afterEvaluate {
		tasks.named("ksp${upperName}Kotlin", KspTaskJvm::class) {
			this.options.add(SubpluginOption("apoption", "firmament.sourceset=${ss.name}"))
		}
		tasks.named("compile${upperName}Kotlin", KotlinCompile::class) {
			this.enabled = isEnabled
		}
		tasks.named("compile${upperName}Java", JavaCompile::class) {
			this.enabled = isEnabled
		}
	}
	compatSourceSets.add(ss)
	loom.createRemapConfigurations(ss)
	if (!isEnabled) {
		ss.output.files.forEach { it.deleteRecursively() }
		return ss
	}
	configurations {
		(ss.implementationConfigurationName) {
			extendsFrom(getByName(mainSS.compileClasspathConfigurationName))
		}
		(ss.annotationProcessorConfigurationName) {
			extendsFrom(getByName(mainSS.annotationProcessorConfigurationName))
		}
		(mainSS.runtimeOnlyConfigurationName) {
			extendsFrom(getByName(ss.runtimeClasspathConfigurationName))
		}
		("ksp$upperName") {
			extendsFrom(ksp.get())
		}
	}
	dependencies {
		runtimeOnly(ss.output)
		(ss.implementationConfigurationName)(sourceSets.main.get().output)
	}
	tasks.shadowJar {
		from(ss.output)
	}
	// TODO: figure out why inheritances are not being respected by tiny kotlin names
	tasks.remapJar {
		classpath.from(configurations.getByName(ss.compileClasspathConfigurationName))
	}
	collectTranslations {
		// TODO: this does not work, somehow
		this.classes.from(sourceSets.main.get().kotlin.classesDirectory)
	}
	return ss
}

val SourceSet.modImplementationConfigurationName
	get() =
		loom.remapConfigurations.find {
			it.targetConfigurationName.get() == this.implementationConfigurationName
		}!!.sourceConfiguration

val shadowMe by configurations.creating {
	exclude(group = "org.jetbrains.kotlin")
	exclude(group = "org.jetbrains.kotlinx")
	exclude(group = "org.jetbrains")
	exclude(module = "gson")
	exclude(group = "org.slf4j")
}
val transInclude by configurations.creating {
	exclude(group = "com.mojang")
	exclude(group = "org.jetbrains.kotlin")
	exclude(group = "org.jetbrains.kotlinx")
	isTransitive = true
}

val hotswap by configurations.creating {
	isVisible = false
}

val nonModImplentation by configurations.creating {
	configurations.implementation.get().extendsFrom(this)
}
val testAgent by configurations.creating {
	isVisible = false
}


val configuredSourceSet = createIsolatedSourceSet("configured",
                                                  isEnabled = false) // Wait for update (also low prio, because configured sucks)
val sodiumSourceSet = createIsolatedSourceSet("sodium", isEnabled = false)
val citResewnSourceSet = createIsolatedSourceSet("citresewn", isEnabled = false) // TODO: Wait for update
val yaclSourceSet = createIsolatedSourceSet("yacl")
val explosiveEnhancementSourceSet =
	createIsolatedSourceSet("explosiveEnhancement", isEnabled = false) // TODO: wait for their port
val wildfireGenderSourceSet = createIsolatedSourceSet("wildfireGender")
val jadeSourceSet = createIsolatedSourceSet("jade")
val modmenuSourceSet = createIsolatedSourceSet("modmenu")
val reiSourceSet = createIsolatedSourceSet("rei")
val moulconfigSourceSet = createIsolatedSourceSet("moulconfig")
val customTexturesSourceSet = createIsolatedSourceSet("texturePacks")

dependencies {
	// Minecraft dependencies
	"minecraft"(libs.minecraft)
	"mappings"("net.fabricmc:yarn:${libs.versions.yarn.get()}:v2")

	// Hotswap Dependency
	hotswap(libs.hotswap)

	// Fabric dependencies
	modImplementation(libs.fabric.loader)
	modImplementation(libs.fabric.kotlin)
	modImplementation(libs.moulconfig)
	modImplementation(libs.manninghamMills)
	modImplementation(libs.basicMath)
	include(libs.basicMath)
	(modmenuSourceSet.modImplementationConfigurationName)(libs.modmenu)
	(explosiveEnhancementSourceSet.modImplementationConfigurationName)(libs.explosiveenhancement)
	modImplementation(libs.hypixelmodapi)
	include(libs.hypixelmodapi.fabric)
	compileOnly(projects.javaplugin)
	annotationProcessor(projects.javaplugin)
	implementation("com.google.auto.service:auto-service-annotations:1.1.1")
	ksp("dev.zacsweers.autoservice:auto-service-ksp:1.2.0")
	include(libs.manninghamMills)
	include(libs.moulconfig)


	annotationProcessor(libs.mixinextras)
	implementation(libs.mixinextras)
	include(libs.mixinextras)

	nonModImplentation(libs.nealisp)
	shadowMe(libs.nealisp)

	modCompileOnly(libs.fabric.api)
	modRuntimeOnly(libs.fabric.api.deprecated)
	modApi(libs.architectury)
	modCompileOnly(libs.jarvis.api)
	include(libs.jarvis.fabric)

	(wildfireGenderSourceSet.modImplementationConfigurationName)(libs.femalegender)
	(wildfireGenderSourceSet.implementationConfigurationName)(customTexturesSourceSet.output)
	(configuredSourceSet.modImplementationConfigurationName)(libs.configured)
	(sodiumSourceSet.modImplementationConfigurationName)(libs.sodium)
	(jadeSourceSet.modImplementationConfigurationName)(libs.jade)

	(citResewnSourceSet.modImplementationConfigurationName)(
		innerJarsOf("citresewn", dependencies.create(libs.citresewn.get()))
	)
	(citResewnSourceSet.modImplementationConfigurationName)(libs.citresewn)
	(yaclSourceSet.modImplementationConfigurationName)(libs.yacl)

	// Actual dependencies
	(reiSourceSet.modImplementationConfigurationName)(libs.rei.api) {
		exclude(module = "architectury")
		exclude(module = "architectury-fabric")
	}
	nonModImplentation(libs.repoparser)
	shadowMe(libs.repoparser)
	fun ktor(mod: String) = "io.ktor:ktor-$mod-jvm:${libs.versions.ktor.get()}"
	// TODO: get rid of ktor. lowkey ballooning file size and like not neccessary at all for what i am doing.0
	transInclude(nonModImplentation(ktor("client-core"))!!)
	transInclude(nonModImplentation(ktor("client-java"))!!)
	transInclude(nonModImplentation(ktor("serialization-kotlinx-json"))!!)
	transInclude(nonModImplentation(ktor("client-content-negotiation"))!!)
	transInclude(nonModImplentation(ktor("client-encoding"))!!)
	transInclude(nonModImplentation(ktor("client-logging"))!!)

	// Dev environment preinstalled mods
	modLocalRuntime(libs.bundles.runtime.required)
	modLocalRuntime(libs.bundles.runtime.optional)
	modLocalRuntime(libs.jarvis.fabric)
	modLocalRuntime(libs.modmenu)

	transInclude.resolvedConfiguration.resolvedArtifacts.forEach {
		include(it.moduleVersion.id.toString())
	}


	testImplementation("io.kotest:kotest-runner-junit5:6.0.0.M1")
	testAgent(project(":testagent", configuration = "shadow"))

	implementation(projects.symbols)
	ksp(projects.symbols)
}

loom {
	clientOnlyMinecraftJar()
	accessWidenerPath.set(project.file("src/main/resources/firmament.accesswidener"))
	runs {
		removeIf { it.name != "client" }
		configureEach {
			property("fabric.log.level", "info")
			property("firmament.debug", "true")
			property("firmament.classroots",
			         compatSourceSets.joinToString(File.pathSeparator) {
				         File(it.output.classesDirs.asPath).absolutePath
			         })
			property("mixin.debug.export", "true")

			parseEnvFile(file(".env")).forEach { (t, u) ->
				environmentVariable(t, u)
			}
			parseEnvFile(file(".properties")).forEach { (t, u) ->
				property(t, u)
			}
		}
		named("client") {
			property("devauth.enabled", "true")
			vmArg("-ea")
//			vmArg("-XX:+AllowEnhancedClassRedefinition")
//			vmArg("-XX:HotswapAgent=external")
//			vmArg("-javaagent:${hotswap.resolve().single().absolutePath}")
		}
	}
}

mcAutoTranslations {
	translationFunction.set("moe.nea.firmament.util.tr")
	translationFunctionResolved.set("moe.nea.firmament.util.trResolved")
}

val downloadTestRepo by tasks.registering(RepoDownload::class) {
	this.hash.set(project.property("firmament.compiletimerepohash") as String)
}

val updateTestRepo by tasks.registering {
	outputs.upToDateWhen { false }
	doLast {
		val propertiesFile = rootProject.file("gradle.properties")
		val json =
			Gson().fromJson(uri("https://api.github.com/repos/NotEnoughUpdates/NotEnoughUpdates-REPO/branches/master")
				                .toURL().readText(), JsonObject::class.java)
		val latestSha = json["commit"].asJsonObject["sha"].asString
		var text = propertiesFile.readText()
		text = text.replace("firmament\\.compiletimerepohash=[^\n]*".toRegex(),
		                    "firmament.compiletimerepohash=$latestSha")
		propertiesFile.writeText(text)
	}
}


tasks.test {
	val wd = file("build/testWorkDir")
	workingDir(wd)
	dependsOn(downloadTestRepo)
	dependsOn(testAgent)
	doFirst {
		wd.mkdirs()
		wd.resolve("config").deleteRecursively()
		systemProperty("firmament.testrepo",
		               downloadTestRepo.flatMap { it.outputDirectory.asFile }.map { it.absolutePath }.get())
		jvmArgs("-javaagent:${testAgent.singleFile.absolutePath}")
	}
	systemProperty("jdk.attach.allowAttachSelf", "true")
	jvmArgs("-XX:+EnableDynamicAgentLoading")
	systemProperties(
		"kotest.framework.classpath.scanning.config.disable" to true,
		"kotest.framework.config.fqn" to "moe.nea.firmament.test.testutil.KotestPlugin",
	)
	useJUnitPlatform()
}


tasks.withType<JavaCompile> {
	this.sourceCompatibility = "21"
	this.targetCompatibility = "21"
	options.encoding = "UTF-8"
	val module = "ALL-UNNAMED"
	options.forkOptions.jvmArgs!!.addAll(listOf(
		"--add-exports=jdk.compiler/com.sun.tools.javac.util=$module",
		"--add-exports=jdk.compiler/com.sun.tools.javac.comp=$module",
		"--add-exports=jdk.compiler/com.sun.tools.javac.tree=$module",
		"--add-exports=jdk.compiler/com.sun.tools.javac.api=$module",
		"--add-exports=jdk.compiler/com.sun.tools.javac.code=$module",
	))
	options.isFork = true
	afterEvaluate {
		options.compilerArgs.add("-Xplugin:IntermediaryNameReplacement mappingFile=${LoomGradleExtension.get(project).mappingsFile.absolutePath} sourceNs=named")
	}
}

tasks.jar {
	destinationDirectory.set(layout.buildDirectory.dir("badjars"))
	archiveClassifier.set("slim")
}

tasks.shadowJar {
	configurations = listOf(shadowMe)
	archiveClassifier.set("dev")
	relocate("io.github.moulberry.repo", "moe.nea.firmament.deps.repo")
	destinationDirectory.set(layout.buildDirectory.dir("badjars"))
	mergeServiceFiles()
}

tasks.remapJar {
//	injectAccessWidener.set(true)
	inputFile.set(tasks.shadowJar.flatMap { it.archiveFile })
	dependsOn(tasks.shadowJar)
	archiveClassifier.set("")
}

tasks.processResources {
	val replacements = listOf(
		"version" to project.version.toString(),
		"minecraft_version" to libs.versions.minecraft.get(),
		"fabric_kotlin_version" to libs.versions.fabric.kotlin.get(),
		"fabric_api_version" to libs.versions.fabric.api.get(),
		"rei_version" to libs.versions.rei.get()
	)
	replacements.forEach { (key, value) -> inputs.property(key, value) }
	filesMatching("**/fabric.mod.json") {
		expand(*replacements.toTypedArray())
	}
	exclude("**/*.license")
	from(tasks.scanLicenses)
	from(collectTranslations) {
		into("assets/firmament/lang")
	}
}

tasks.scanLicenses {
	scanConfiguration(nonModImplentation)
	scanConfiguration(configurations.modCompileClasspath.get())
	outputFile.set(layout.buildDirectory.file("LICENSES-FIRMAMENT.json"))
	licenseFormatter.set(moe.nea.licenseextractificator.JsonLicenseFormatter())
}
tasks.create("printAllLicenses", LicenseDiscoveryTask::class.java, licensing).apply {
	outputFile.set(layout.buildDirectory.file("LICENSES-FIRMAMENT.txt"))
	licenseFormatter.set(moe.nea.licenseextractificator.TextLicenseFormatter())
	scanConfiguration(nonModImplentation)
	scanConfiguration(configurations.modCompileClasspath.get())
	doLast {
		println(outputFile.get().asFile.readText())
	}
	outputs.upToDateWhen { false }
}
fun patchRenderDoc(
	javaLauncher: JavaLauncher,
): JavaLauncher {
	val wrappedJavaExecutable = javaLauncher.executablePath.asFile.absolutePath
	require("\"" !in wrappedJavaExecutable)
	val hashBytes = Hashing.sha256().hashString(wrappedJavaExecutable, StandardCharsets.UTF_8)
	val hash = Base64.getUrlEncoder().encodeToString(hashBytes.asBytes())
		.replace("=", "")
	val wrapperJavaRoot = rootProject.layout.buildDirectory
		.dir("binaries/renderdoc-wrapped-java/$hash/")
		.get()
	val isWindows = Os.isFamily(Os.FAMILY_WINDOWS)
	val wrapperJavaExe =
		if (isWindows) wrapperJavaRoot.file("java.cmd")
		else wrapperJavaRoot.file("java")
	return object : JavaLauncher {
		override fun getMetadata(): JavaInstallationMetadata {
			return object : JavaInstallationMetadata by javaLauncher.metadata {
				override fun isCurrentJvm(): Boolean {
					return false
				}
			}
		}

		override fun getExecutablePath(): RegularFile {
			val fileF = wrapperJavaExe.asFile
			if (!fileF.exists()) {
				fileF.parentFile.mkdirs()
				if (isWindows) {
					fileF.writeText("""
					setlocal enableextensions
					start "" renderdoccmd.exe capture --opt-hook-children --wait-for-exit --working-dir . "$wrappedJavaExecutable" %*
					endlocal
					""".trimIndent())
				} else {
					fileF.writeText("""
					#!/usr/bin/env bash
					exec renderdoccmd capture --opt-hook-children --wait-for-exit --working-dir . "$wrappedJavaExecutable" "$@"
					""".trimIndent())
					fileF.setExecutable(true)
				}
			}
			return wrapperJavaExe
		}
	}
}
tasks.runClient {
	javaLauncher.set(javaToolchains.launcherFor(java.toolchain).map { patchRenderDoc(it) })
}
//tasks.register<Exec>("runRenderDoc") {
//	val runClient = tasks.runClient.get()
//	commandLine(
//		"renderdoc",
//		"capture",
//		"--opt-hook-children",
//		"--wait-for-exit",
//		"--working-dir",
//		runClient.workingDir,
//		runClient.javaLauncher.get().executablePath.asFile.absoluteFile,
//	)
//	args(runClient.allJvmArgs)
//	args()
//
//}

tasks.withType<AbstractArchiveTask>().configureEach {
	isPreserveFileTimestamps = false
	isReproducibleFileOrder = true
}

licensing.addExtraLicenseMatchers()
