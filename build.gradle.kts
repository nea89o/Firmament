/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: CC0-1.0
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.google.devtools.ksp.gradle.KspAATask
import com.google.gson.Gson
import com.google.gson.JsonObject
import moe.nea.licenseextractificator.LicenseDiscoveryTask
import moe.nea.mcautotranslations.gradle.CollectTranslations
import org.apache.tools.ant.taskdefs.condition.Os
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.utils.extendsFrom
import java.util.*

plugins {
	java
	`maven-publish`
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.plugin.serialization)
	alias(libs.plugins.kotlin.plugin.powerassert)
	alias(libs.plugins.kotlin.plugin.ksp)
	id("firmament.common")
	id("firmament.loom")
	id("firmament.license-management")
	alias(libs.plugins.mcAutoTranslations)
}

version = getGitTagInfo(libs.versions.minecraft.get())

java {
	withSourcesJar()
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(25))
	}
}

val javaLanguageVersion = java.toolchain.languageVersion
val javaLanguageVersionName = javaLanguageVersion.map { it.toString() }
val javaLanguageVersionJvmTarget = javaLanguageVersionName.map { JvmTarget.fromTarget(it) }

loom {
	mixin.useLegacyMixinAp.set(false)
}

tasks.withType(KotlinCompile::class) {
	compilerOptions {
		jvmTarget.set(javaLanguageVersionJvmTarget)
	}
}

kotlin {
	sourceSets.all {
		languageSettings {
//		TODO: they took away my toys	enableLanguageFeature("BreakContinueInInlineLambdas")
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
	this.baseTranslations.from(file("translations/extra.json"))
	this.classes.from(sourceSets.main.get().kotlin.classesDirectory)
}

val shadowJar = tasks.register("shadowJar", ShadowJar::class)
val mergedSourceSetsJar = tasks.register("mergedSourceSetsJar", ShadowJar::class)

val compatSourceSets: MutableSet<SourceSet> = mutableSetOf()
fun createIsolatedSourceSet(
	name: String,
	path: String = "compat/$name",
	isEnabled: Boolean = true,
	inheritsFromMain: Boolean = true,
	enableKsp: Boolean = true,
): SourceSet {
	val ss = sourceSets.create(name) {
		this.java.setSrcDirs(listOf(layout.projectDirectory.dir("src/$path/java")))
		this.kotlin.setSrcDirs(listOf(layout.projectDirectory.dir("src/$path/java")))
	}
	val mainSS = sourceSets.main.get()
	val upperName = ss.name.capitalizeN()
	afterEvaluate {
		tasks.named("ksp${upperName}Kotlin", KspAATask::class) {
			this.commandLineArgumentProviders.add { // TODO: update https://github.com/google/ksp/issues/2075
				listOf("firmament.sourceset=${ss.name}")
			}
			if (!enableKsp)
				this.enabled = false
		}
		tasks.named("compile${upperName}Kotlin", KotlinCompile::class) {
			this.enabled = isEnabled
		}
		tasks.named("compile${upperName}Java", JavaCompile::class) {
			this.enabled = isEnabled
		}
	}
	compatSourceSets.add(ss)
	if (!isEnabled) {
		ss.output.files.forEach { it.deleteRecursively() }
		return ss
	}
	configurations {
		(ss.compileOnlyConfigurationName) {
			if (inheritsFromMain) {
				// TODO: if not inheriting from main maybe inherit minecraft
				extendsFrom(configurations.named(mainSS.compileClasspathConfigurationName))
				// Inherit from individual configurations to avoid inheriting transitively from minecraftNamedCompile
//				extendsFrom(getByName(mainSS.implementationConfigurationName))
//				extendsFrom(getByName(mainSS.compileOnlyConfigurationName))
			}
		}
		(ss.annotationProcessorConfigurationName) {
			extendsFrom(getByName(mainSS.annotationProcessorConfigurationName))
		}
		(mainSS.runtimeOnlyConfigurationName) {
			if (isEnabled)
				extendsFrom(getByName(ss.runtimeClasspathConfigurationName))
		}
		if (enableKsp)
			("ksp$upperName") {
				extendsFrom(ksp.get())
			}
	}
	dependencies {
		if (isEnabled)
			runtimeOnly(ss.output)
		if (inheritsFromMain) {
			(ss.implementationConfigurationName)(project.files(tasks.compileKotlin.map { it.destinationDirectory }))
			(ss.implementationConfigurationName)(project.files(tasks.compileJava.map { it.destinationDirectory }))
		}
	}
	mergedSourceSetsJar.configure {
		from(ss.output)
	}
	collectTranslations {
		this.classes.from(ss.kotlin.classesDirectory)
	}
	return ss
}

@Deprecated("No more remapping")
val SourceSet.modImplementationConfigurationName
	get() =
		this.implementationConfigurationName

@Deprecated("No more remapping")
val SourceSet.modRuntimeOnlyConfigurationName
	get() =
		this.runtimeOnlyConfigurationName

val shadowMe by configurations.creating {
	exclude(group = "org.jetbrains.kotlin")
	exclude(group = "org.jetbrains.kotlinx")
	exclude(group = "org.jetbrains")
	exclude(module = "gson")
	exclude(group = "org.slf4j")
}

val hotswap by configurations.creating {}

val testAgent by configurations.creating {}

fabricApi.configureTests {
	createSourceSet.set(true)
	enableClientGameTests.set(true)
	modId.set("firmament-gametest")
	eula.set(true)
	username.set("CoolGuy123")
}
val gameTestSourceSet by sourceSets.named("gametest") {
	configurations.named(compileClasspathConfigurationName).extendsFrom(configurations.testCompileClasspath)
	configurations.named(runtimeClasspathConfigurationName).extendsFrom(configurations.testRuntimeClasspath)
}

val sodiumSourceSet = createIsolatedSourceSet("sodium")
val yaclSourceSet = createIsolatedSourceSet("yacl")
val wildfireGenderSourceSet = createIsolatedSourceSet("wildfireGender")
val jadeSourceSet = createIsolatedSourceSet("jade")
val modmenuSourceSet = createIsolatedSourceSet("modmenu")
val reiSourceSet = createIsolatedSourceSet("rei")
val moulconfigSourceSet = createIsolatedSourceSet("moulconfig")
val irisSourceSet = createIsolatedSourceSet("iris")
val customTexturesSourceSet = createIsolatedSourceSet("texturePacks", "texturePacks")
val apiSourceSet = createIsolatedSourceSet("api", "api", inheritsFromMain = false, enableKsp = false)
val jarvisSourceSet = createIsolatedSourceSet("jarvis", "vendor/jarvis", inheritsFromMain = false, enableKsp = false)

dependencies {
	// Minecraft dependencies
	"minecraft"(libs.minecraft)

	// Hotswap Dependency
	hotswap(libs.hotswap)

	// Fabric dependencies
	implementation(libs.fabric.loader)
	implementation(libs.fabric.kotlin)
	implementation(libs.moulconfig)
	implementation(libs.manninghamMills)
	implementation(libs.basicMath)
	implementation(apiSourceSet.output)
	implementation(jarvisSourceSet.output)
	// We need to depend via compileOnly to avoid DeobfSpecContext.getModsFromConfiguration from instantiating minecraft
	// during preparation of the minecraft dependency, creating a circular dependency
	(apiSourceSet.compileOnlyConfigurationName)(loom.namedMinecraftJars)
	(apiSourceSet.implementationConfigurationName)(libs.jspecify)
	(apiSourceSet.implementationConfigurationName)(libs.jbAnnotations)
	(jarvisSourceSet.compileOnlyConfigurationName)(loom.namedMinecraftJars)
	configurations.named(jarvisSourceSet.compileOnlyConfigurationName) {
		extendsFrom(configurations.named("minecraftLibraries"))
	}
	(jarvisSourceSet.implementationConfigurationName)(libs.jspecify)
	(jarvisSourceSet.implementationConfigurationName)(libs.jbAnnotations)
	(jarvisSourceSet.implementationConfigurationName)(libs.fabric.api)
	(jarvisSourceSet.implementationConfigurationName)(libs.fabric.loader)
	include(libs.basicMath)
	(modmenuSourceSet.implementationConfigurationName)(libs.modmenu)
	implementation(libs.hypixelmodapi)
	runtimeOnly(libs.hypixelmodapi.fabric)
	include(libs.hypixelmodapi.fabric)
	compileOnly(projects.javaplugin)
	annotationProcessor(projects.javaplugin)
	implementation("com.google.auto.service:auto-service-annotations:1.1.1")
	ksp("dev.zacsweers.autoservice:auto-service-ksp:1.2.0")
	include(libs.manninghamMills)
	shadowMe(libs.moulconfig)

	annotationProcessor(libs.mixinextras)
	implementation(libs.mixinextras)
	include(libs.mixinextras)

	implementation(libs.nealisp)
	shadowMe(libs.nealisp)

	implementation(libs.fabric.api)
	runtimeOnly(libs.fabric.api.deprecated)

	(irisSourceSet.implementationConfigurationName)(libs.iris)
	(wildfireGenderSourceSet.implementationConfigurationName)(libs.femalegender)
	(wildfireGenderSourceSet.implementationConfigurationName)(customTexturesSourceSet.output)
	(sodiumSourceSet.implementationConfigurationName)(libs.sodium)
	(sodiumSourceSet.implementationConfigurationName)(customTexturesSourceSet.output)
	(jadeSourceSet.implementationConfigurationName)(libs.jade)

	(yaclSourceSet.implementationConfigurationName)(libs.yacl)

	// Actual dependencies
	val reiDeps = libs.rei
	(reiSourceSet.implementationConfigurationName)(reiDeps.api)
	(reiSourceSet.implementationConfigurationName)(reiDeps.fabric)
	implementation(libs.repoparser)
	shadowMe(libs.repoparser)

	// Dev environment preinstalled mods
	runtimeOnly(libs.devauth)
	runtimeOnly(libs.modmenu)
	runtimeOnly(libs.noChatRestrictions)

	testImplementation("net.fabricmc:fabric-loader-junit:${libs.versions.fabric.loader.get()}")
	testAgent(files(tasks.getByPath(":testagent:jar")))

	"gametestImplementation"(sourceSets.test.map { it.output })

	implementation(projects.symbols)
	ksp(projects.symbols)
}

loom {
	clientOnlyMinecraftJar()
	accessWidenerPath.set(project.file("src/main/resources/firmament.accesswidener"))
	runs {
		removeIf { it.name == "server" }
		configureEach {
			property("fabric.log.level", "info")
			property("firmament.debug", "true")
			property(
				"firmament.classroots",
				compatSourceSets.joinToString(File.pathSeparator) {
					File(it.output.classesDirs.asPath).absolutePath
				})
			property("mixin.debug.export", "true")
//			property("mixin.debug", "true")

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
			if (project.findProperty("firmament.useHotswap") == "true") {
				vmArg("-XX:+AllowEnhancedClassRedefinition")
				vmArg("-XX:HotswapAgent=external")
				vmArg("-javaagent:${hotswap.resolve().single().absolutePath}")
			}
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
			Gson().fromJson(
				uri("https://api.github.com/repos/NotEnoughUpdates/NotEnoughUpdates-REPO/branches/master")
					.toURL().readText(), JsonObject::class.java
			)
		val latestSha = json["commit"].asJsonObject["sha"].asString
		var text = propertiesFile.readText()
		text = text.replace(
			"firmament\\.compiletimerepohash=[^\n]*".toRegex(),
			"firmament.compiletimerepohash=$latestSha"
		)
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
		systemProperty(
			"firmament.testrepo",
			downloadTestRepo.flatMap { it.outputDirectory.asFile }.map { it.absolutePath }.get()
		)
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
	this.sourceCompatibility = javaLanguageVersionName.get()
	this.targetCompatibility = javaLanguageVersionName.get()
	options.encoding = "UTF-8"
	val module = "ALL-UNNAMED"
	options.forkOptions.jvmArgs!!.addAll(
		listOf(
			"--add-exports=jdk.compiler/com.sun.tools.javac.util=$module",
			"--add-exports=jdk.compiler/com.sun.tools.javac.comp=$module",
			"--add-exports=jdk.compiler/com.sun.tools.javac.tree=$module",
			"--add-exports=jdk.compiler/com.sun.tools.javac.api=$module",
			"--add-exports=jdk.compiler/com.sun.tools.javac.code=$module",
		)
	)
	options.isFork = true
	afterEvaluate {
		options.compilerArgs.add("-Xplugin:IntermediaryNameReplacement")
	}
}

fun AbstractArchiveTask.badjar() = destinationDirectory.set(layout.buildDirectory.dir("badjars"))
tasks.jar {
	badjar()
	archiveClassifier.set("slim")
}
val apiJar = tasks.register("apiJar", Jar::class) {
	archiveClassifier = "api"
	from(apiSourceSet.output)
}

val apiSourcesJar = tasks.register("apiSourcesJar", Jar::class) {
	from(apiSourceSet.allSource)
	archiveClassifier = "api-sources"
}

tasks.assemble {
	dependsOn(apiJar)
	dependsOn(apiSourcesJar)
}


/**
 * This configures a shadow jar task to allow duplicate entries before transformation through [mergeServiceFiles]
 * or [transform], but to fail if those transformers don't collapse all duplicates.
 */
fun ShadowJar.configureDuplicateStrategy() {
	duplicatesStrategy = DuplicatesStrategy.INCLUDE
	failOnDuplicateEntries = true
}

mergedSourceSetsJar.configure {
	from(zipTree(tasks.jar.flatMap { it.archiveFile }))
	badjar()
	archiveClassifier.set("merged-source-sets")
	mergeServiceFiles()
	configureDuplicateStrategy()
}

shadowJar.configure { // TODO: can these two shadowJar tasks be merged
	from(zipTree(mergedSourceSetsJar.flatMap { it.archiveFile }))
	configurations = listOf(shadowMe)
	configureDuplicateStrategy()
	archiveClassifier.set("")
	relocate("io.github.moulberry.repo", "moe.nea.firmament.deps.repo")
	relocate("io.github.notenoughupdates.moulconfig", "moe.nea.firmament.deps.moulconfig")
	mergeServiceFiles()
	transform<FabricModTransform>()
}

tasks.assemble { dependsOn(shadowJar) }


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
	from(tasks.scanLicenses) // TODO: reinstate this
	from(collectTranslations) {
		into("assets/firmament/lang")
	}
	from(project.files("translations/languages/")) {
		into("assets/firmament/lang")
	}
}

tasks.scanLicenses {
//	scanConfiguration(configurations.implementation.get()) // TODO: i should change this upstream to allow for providers
//	compatSourceSets.forEach { // TODO: this is a sham
//		scanConfiguration(it.modImplementationConfigurationName.get())
//	}
	outputFile.set(layout.buildDirectory.file("LICENSES-FIRMAMENT.json"))
	licenseFormatter.set(moe.nea.licenseextractificator.JsonLicenseFormatter())
}
tasks.register("printAllLicenses", LicenseDiscoveryTask::class.java, licensing).configure {
	outputFile.set(layout.buildDirectory.file("LICENSES-FIRMAMENT.txt"))
	licenseFormatter.set(moe.nea.licenseextractificator.TextLicenseFormatter())
//	compatSourceSets.forEach {
//		scanConfiguration(it.modImplementationConfigurationName.get())
//	}
//	scanConfiguration(nonModImplentation)
//	scanConfiguration(configurations.modCompileClasspath.get())
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
	val hashBytes = wrappedJavaExecutable.encodeToByteArray()
	val hash = Base64.getUrlEncoder().encodeToString(hashBytes)
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
					fileF.writeText(
						"""
					setlocal enableextensions
					start "" renderdoccmd.exe capture --opt-hook-children --wait-for-exit --working-dir . "$wrappedJavaExecutable" %*
					endlocal
					""".trimIndent()
					)
				} else {
					fileF.writeText(
						"""
					#!/usr/bin/env bash
					exec renderdoccmd capture --opt-hook-children --wait-for-exit --working-dir . "$wrappedJavaExecutable" "$@"
					""".trimIndent()
					)
					fileF.setExecutable(true)
				}
			}
			return wrapperJavaExe
		}
	}
}
tasks.runClient {
	javaLauncher.set(
		javaToolchains.launcherFor(java.toolchain)
//			.map { patchRenderDoc(it) }
	)
}

tasks.withType<AbstractArchiveTask>().configureEach {
	isPreserveFileTimestamps = false
	isReproducibleFileOrder = true
}
