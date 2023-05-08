import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    `maven-publish`
    kotlin("jvm") version "1.8.10"
    kotlin("plugin.serialization") version "1.8.10"
    id("dev.architectury.loom") version "1.1.336"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("moe.nea.licenseextractificator") version "fffc76c"
    id("io.github.juuxel.loom-quiltflower") version "1.7.3"
}

loom {
    clientOnlyMinecraftJar()
    accessWidenerPath.set(project.file("src/main/resources/notenoughupdates.accesswidener"))
    runs {
        removeIf { it.name != "client" }
        named("client") {
            property("devauth.enabled", "true")
            property("fabric.log.level", "info")
            property("notenoughupdates.debug", "true")
            /*
            vmArg("-XX:+AllowEnhancedClassRedefinition")
            vmArg("-XX:HotswapAgent=fatjar")
             */
        }
    }
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
    maven("https://server.bbkr.space/artifactory/libs-release")
    maven("https://repo.nea.moe/releases")
    mavenLocal()
}

val shadowMe by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

val transInclude by configurations.creating {
    exclude(group = "com.mojang")
    exclude(group = "org.jetbrains.kotlin")
    exclude(group = "org.jetbrains.kotlinx")
    isTransitive = true
}

dependencies {
    // Minecraft dependencies
    "minecraft"(libs.minecraft)
    "mappings"("net.fabricmc:yarn:${libs.versions.yarn.get()}:v2")

    // Fabric dependencies
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.kotlin)
    modImplementation(libs.libgui)
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

    transInclude(implementation(ktor("client-core"))!!)
    transInclude(implementation(ktor("client-java"))!!)
    transInclude(implementation(ktor("serialization-kotlinx-json"))!!)
    transInclude(implementation(ktor("client-content-negotiation"))!!)

    // Dev environment preinstalled mods
    modRuntimeOnly(libs.bundles.runtime.required)
    modRuntimeOnly(libs.bundles.runtime.optional)

    transInclude.resolvedConfiguration.resolvedArtifacts.forEach {
        include(it.moduleVersion.id.toString())
    }
}


version = rootProject.property("mod_version").toString()
group = rootProject.property("maven_group").toString()

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(17)
}

java {
    withSourcesJar()
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

// could not set to 17, up to 16
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "16"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "16"
}

tasks.shadowJar {
    configurations = listOf(shadowMe)
    archiveClassifier.set("dev-thicc")
}

tasks.remapJar {
    injectAccessWidener.set(true)
    inputFile.set(tasks.shadowJar.flatMap { it.archiveFile })
    dependsOn(tasks.shadowJar)
    archiveClassifier.set("thicc")
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
    filesMatching("**/lang/*.json") {
        // flattenJson(this)
    }
    from(tasks.license)
}


tasks.license {
    scanConfiguration(project.configurations.compileClasspath.get())
    outputFile.set(file("$buildDir/LICENSES-NEU.json"))
    licenseFormatter.set(moe.nea.licenseextractificator.JsonLicenseFormatter())
}
