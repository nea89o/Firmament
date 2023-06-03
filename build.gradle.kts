import moe.nea.licenseextractificator.LicenseDiscoveryTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    `maven-publish`
    kotlin("jvm") version "1.8.10"
    kotlin("plugin.serialization") version "1.8.10"
    id("dev.architectury.loom") version "1.1.336"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("moe.nea.licenseextractificator")
    id("io.github.juuxel.loom-quiltflower") version "1.10.0"
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
    mavenLocal()
}

val shadowMe by configurations.creating
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
    extendsFrom(shadowMe)
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
    modImplementation(libs.lib39.core)
    include(libs.lib39.core)
    include(libs.libgui)
    modApi(libs.fabric.api)
    modApi(libs.architectury)

    // Actual dependencies
    modCompileOnly(libs.rei.api) {
        exclude(module = "architectury")
        exclude(module = "architectury-fabric")
    }
    shadowMe(libs.repoparser)
    shadowMe(libs.bundles.dbus)

    fun ktor(mod: String) = "io.ktor:ktor-$mod-jvm:${libs.versions.ktor.get()}"

    transInclude(nonModImplentation(ktor("client-core"))!!)
    transInclude(nonModImplentation(ktor("client-java"))!!)
    transInclude(nonModImplentation(ktor("serialization-kotlinx-json"))!!)
    transInclude(nonModImplentation(ktor("client-content-negotiation"))!!)
    transInclude(nonModImplentation(ktor("client-encoding"))!!)
    transInclude(nonModImplentation(ktor("client-logging"))!!)

    // Dev environment preinstalled mods
    modRuntimeOnly(libs.bundles.runtime.required)
    modRuntimeOnly(libs.bundles.runtime.optional)

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
    archiveClassifier.set("")
    relocate("io.github.moulberry.repo", "moe.nea.firmament.deps.repo")
}

tasks.remapJar {
    destinationDirectory.set(layout.buildDirectory.dir("badjars"))
    injectAccessWidener.set(true)
    inputFile.set(tasks.shadowJar.flatMap { it.archiveFile })
    dependsOn(tasks.shadowJar)
    archiveClassifier.set("dev")
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
