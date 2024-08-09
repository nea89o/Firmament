/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: CC0-1.0
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

import moe.nea.licenseextractificator.LicenseDiscoveryTask
import net.fabricmc.loom.LoomGradleExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    `maven-publish`
    id("com.google.devtools.ksp") version "1.9.23-1.0.20"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
//    id("com.bnorm.power.kotlin-power-assert") version "0.13.0"
    id("dev.architectury.loom") version "1.6.397"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("moe.nea.licenseextractificator")
//    id("io.github.juuxel.loom-vineflower") version "1.11.0"
}

java {
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "21"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "21"
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
        mavenLocal()
    }
}
kotlin {
    sourceSets.all {
        languageSettings {
//            languageVersion = "2.0"
            enableLanguageFeature("BreakContinueInInlineLambdas")
        }
    }
}

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
    compileOnly(project(":javaplugin"))
    annotationProcessor(project(":javaplugin"))
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
    modCompileOnly(libs.sodium)
    include(libs.jarvis.fabric)

    modCompileOnly(libs.femalegender)

    // Actual dependencies
    modCompileOnly(libs.rei.api) {
        exclude(module = "architectury")
        exclude(module = "architectury-fabric")
    }
    nonModImplentation(libs.repoparser)
    shadowMe(libs.repoparser)
    fun ktor(mod: String) = "io.ktor:ktor-$mod-jvm:${libs.versions.ktor.get()}"

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

version = getGitTagInfo()
group = rootProject.property("maven_group").toString()

loom {
    clientOnlyMinecraftJar()
    accessWidenerPath.set(project.file("src/main/resources/firmament.accesswidener"))
    runs {
        removeIf { it.name != "client" }
        named("client") {
            property("devauth.enabled", "true")
            property("fabric.log.level", "info")
            property("firmament.debug", "true")
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
