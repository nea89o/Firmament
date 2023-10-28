// SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
//
// SPDX-License-Identifier: CC0-1.0

import moe.nea.licenseextractificator.LicenseDiscoveryTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    `maven-publish`
    kotlin("jvm") version "1.8.20"
    kotlin("plugin.serialization") version "1.8.20"
    id("com.bnorm.power.kotlin-power-assert") version "0.13.0"
    id("dev.architectury.loom") version "1.1.336"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("moe.nea.licenseextractificator")
    id("io.github.juuxel.loom-vineflower") version "1.11.0"
    id("io.shcm.shsupercm.fabric.fletchingtable") version "1.5"
}

java {
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "17"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "17"
}

repositories {
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://maven.shedaniel.me")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://api.modrinth.com/maven") {
        content {
            includeGroup("maven.modrinth")
        }
    }
    maven("https://jitpack.io/") {
        content {
            includeGroupByRegex("(com|io)\\.github\\..+")
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
    mavenLocal()
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
    modImplementation(libs.libgui)
    modImplementation(libs.moulconfig)
    modCompileOnly(libs.explosiveenhancement)
    include(libs.libgui)
    include(libs.moulconfig)


    annotationProcessor(libs.mixinextras)
    implementation(libs.mixinextras)
    include(libs.mixinextras)

    nonModImplentation(libs.nealisp)
    shadowMe(libs.nealisp)

    modApi(libs.fabric.api)
    modApi(libs.architectury)
    modCompileOnly(libs.jarvis.api)
    include(libs.jarvis.fabric)

    // Actual dependencies
    modCompileOnly(libs.rei.api) {
        exclude(module = "architectury")
        exclude(module = "architectury-fabric")
    }
    nonModImplentation(libs.repoparser)
    shadowMe(libs.repoparser)
    nonModImplentation(libs.bundles.dbus)
    shadowMe(libs.bundles.dbus)

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
}


version = rootProject.property("mod_version").toString()
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

            vmArg("-ea")
            vmArg("-XX:+AllowEnhancedClassRedefinition")
            vmArg("-XX:HotswapAgent=external")
            vmArg("-javaagent:${hotswap.resolve().single().absolutePath}")
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(17)
}

tasks.jar {
    destinationDirectory.set(layout.buildDirectory.dir("badjars"))
    archiveClassifier.set("slim")
}

tasks.shadowJar {
    configurations = listOf(shadowMe)
    archiveClassifier.set("dev")
    doLast {
        configurations.forEach {
            println("Copying files into jar: ${it.files}")
        }
    }
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
        "version" to project.version,
        "minecraft_version" to libs.versions.minecraft.get(),
        "fabric_kotlin_version" to libs.versions.fabric.kotlin.get()
    ).map { (k, v) -> k to v.toString() }
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
    outputFile.set(file("$buildDir/LICENSES-FIRMAMENT.json"))
    licenseFormatter.set(moe.nea.licenseextractificator.JsonLicenseFormatter())
}
tasks.create("printAllLicenses", LicenseDiscoveryTask::class.java, licensing).apply {
    outputFile.set(file("$buildDir/LICENSES-FIRMAMENT.txt"))
    licenseFormatter.set(moe.nea.licenseextractificator.TextLicenseFormatter())
    scanConfiguration(nonModImplentation)
    scanConfiguration(configurations.modCompileClasspath.get())
    doLast {
        println(outputFile.get().asFile.readText())
    }
    outputs.upToDateWhen { false }
}

licensing.addExtraLicenseMatchers()

fletchingTable.defaultMixinEnvironment.set("client")

vineflower {
    toolVersion.set("1.9.1")
}
