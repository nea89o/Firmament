/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: CC0-1.0
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

import com.google.devtools.ksp.gradle.KspTaskJvm
import moe.nea.licenseextractificator.LicenseDiscoveryTask
import net.fabricmc.loom.LoomGradleExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    `maven-publish`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.kotlin.plugin.powerassert)
    alias(libs.plugins.kotlin.plugin.ksp)
    alias(libs.plugins.loom)
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("moe.nea.licenseextractificator")
}

version = getGitTagInfo()
group = rootProject.property("maven_group").toString()

java {
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
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
val compatSourceSets: MutableSet<SourceSet> = mutableSetOf()
fun createIsolatedSourceSet(name: String, path: String = "compat/$name"): SourceSet {
    val ss = sourceSets.create(name) {
        this.java.setSrcDirs(listOf(layout.projectDirectory.dir("src/$path/java")))
        this.kotlin.setSrcDirs(listOf(layout.projectDirectory.dir("src/$path/java")))
    }
    compatSourceSets.add(ss)
    loom.createRemapConfigurations(ss)
    val mainSS = sourceSets.main.get()
    val upperName = ss.name.replaceFirstChar { it.uppercaseChar() }
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
    afterEvaluate {
        tasks.named("ksp${upperName}Kotlin", KspTaskJvm::class) {
            this.options.add(SubpluginOption("apoption", "firmament.sourceset=${ss.name}"))
        }
    }
    dependencies {
        runtimeOnly(ss.output)
        (ss.implementationConfigurationName)(sourceSets.main.get().output)
    }
    tasks.shadowJar {
        from(ss.output)
    }
    return ss
}

val SourceSet.modImplementationConfigurationName
    get() =
        loom.remapConfigurations.find {
            it.targetConfigurationName.get() == this.implementationConfigurationName
        }!!.sourceConfiguration
val configuredSourceSet = createIsolatedSourceSet("configured")
val sodiumSourceSet = createIsolatedSourceSet("sodium")

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

loom {
}

dependencies {
    // Minecraft dependencies
    "minecraft"(libs.minecraft)
    "mappings"("net.fabricmc:yarn:${libs.versions.yarn.get()}:v2")

    // Hotswap Dependency
    hotswap(libs.hotswap)

    // Fabric dependencies
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.kotlin)
    modImplementation(libs.modmenu)
    modImplementation(libs.moulconfig)
    modImplementation(libs.manninghamMills)
    modCompileOnly(libs.explosiveenhancement)
    modImplementation(libs.hypixelmodapi)
    include(libs.hypixelmodapi.fabric)
    compileOnly(project(":javaplugin"))
    annotationProcessor(project(":javaplugin"))
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

    modCompileOnly(libs.femalegender)
    (configuredSourceSet.modImplementationConfigurationName)(libs.configured)
    (sodiumSourceSet.modImplementationConfigurationName)(libs.sodium)

    // Actual dependencies
    modCompileOnly(libs.rei.api) {
        exclude(module = "architectury")
        exclude(module = "architectury-fabric")
    }
    nonModImplentation(libs.repoparser)
    shadowMe(libs.repoparser)
    fun ktor(mod: String) = "io.ktor:ktor-$mod-jvm:${libs.versions.ktor.get()}"
    modCompileOnly(libs.citresewn)
    transInclude(nonModImplentation(ktor("client-core"))!!)
    transInclude(nonModImplentation(ktor("client-java"))!!)
    transInclude(nonModImplentation(ktor("serialization-kotlinx-json"))!!)
    transInclude(nonModImplentation(ktor("client-content-negotiation"))!!)
    transInclude(nonModImplentation(ktor("client-encoding"))!!)
    transInclude(nonModImplentation(ktor("client-logging"))!!)

    // Dev environment preinstalled mods
    modLocalRuntime(libs.bundles.runtime.required)
    modLocalRuntime(libs.bundles.runtime.optional)
    modImplementation(modLocalRuntime(project.files("citresewn-defaults-1.2.0+1.21.jar"))!!)
    modLocalRuntime(libs.jarvis.fabric)

    transInclude.resolvedConfiguration.resolvedArtifacts.forEach {
        include(it.moduleVersion.id.toString())
    }


    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")

    implementation(project(":symbols"))
    ksp(project(":symbols"))
}

tasks.test {
    useJUnitPlatform()
}

loom {
    clientOnlyMinecraftJar()
    accessWidenerPath.set(project.file("src/main/resources/firmament.accesswidener"))
    runs {
        removeIf { it.name != "client" }
        named("client") {
            property("devauth.enabled", "true")
            property("fabric.log.level", "info")
            property("firmament.debug", "true")
            property("firmament.classroots",
                     compatSourceSets.joinToString(File.pathSeparator) {
                         File(it.output.classesDirs.asPath).absolutePath
                     })
            property("mixin.debug", "true")

            parseEnvFile(file(".env")).forEach { (t, u) ->
                environmentVariable(t, u)
            }
            parseEnvFile(file(".properties")).forEach { (t, u) ->
                property(t, u)
            }
            vmArg("-ea")
            vmArg("-XX:+AllowEnhancedClassRedefinition")
            vmArg("-XX:HotswapAgent=external")
            vmArg("-javaagent:${hotswap.resolve().single().absolutePath}")
        }
    }
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
    injectAccessWidener.set(true)
    inputFile.set(tasks.shadowJar.flatMap { it.archiveFile })
    dependsOn(tasks.shadowJar)
    archiveClassifier.set("")
}

tasks.processResources {
    val replacements = listOf(
        "version" to project.version.toString(),
        "minecraft_version" to libs.versions.minecraft.get(),
        "fabric_kotlin_version" to libs.versions.fabric.kotlin.get(),
        "rei_version" to libs.versions.rei.get()
    )
    replacements.forEach { (key, value) -> inputs.property(key, value) }
    filesMatching("**/fabric.mod.json") {
        expand(*replacements.toTypedArray())
    }
    exclude("**/*.license")
    from(tasks.scanLicenses)
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

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

licensing.addExtraLicenseMatchers()
