plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

architectury {
    platformSetupLoomIde()
    forge()
}

loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)

    forge {
        convertAccessWideners.set(true)
        extraAccessWideners.add(loom.accessWidenerPath.get().asFile.name)
    }
}

/**
 * @see: https://docs.gradle.org/current/userguide/migrating_from_groovy_to_kotlin_dsl.html
 * */
val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating // Don't use shadow from the shadow plugin because we don't want IDEA to index this.
val developmentForge: Configuration = configurations.getByName("developmentForge")
configurations {
    compileClasspath.get().extendsFrom(configurations["common"])
    runtimeClasspath.get().extendsFrom(configurations["common"])
    developmentForge.extendsFrom(configurations["common"])
}

dependencies {
    forge("net.minecraftforge:forge:${rootProject.property("forge_version")}")
    // Remove the next line if you don't want to depend on the API
    modApi("dev.architectury:architectury-forge:${rootProject.property("architectury_version")}")
    common(project(":common", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(":common", configuration = "transformProductionForge")) { isTransitive = false }
    common(kotlin("stdlib-jdk8"))
    modRuntimeOnly("me.shedaniel:RoughlyEnoughItems-forge:${rootProject.property("rei_version")}")
}

val javaComponent = components["java"] as AdhocComponentWithVariants
javaComponent.withVariantsFromConfiguration(configurations["sourcesElements"]) {
    skip()
}

tasks {
    processResources {
        inputs.property("version", project.version)

        filesMatching("META-INF/mods.toml") {
            expand("version" to project.version)
        }
    }

    shadowJar {
        exclude("fabric.mod.json")
        exclude("architectury.common.json")

        /**
         * magic!
         * groovy -> kotlin dsl
         * [project.configurations.shadowCommon] -> listOf(project.configurations["shadowCommon"])
         * */
        configurations = listOf(project.configurations["shadowCommon"])
        archiveClassifier.set("dev-shadow")
    }

    remapJar {
        /**
         * magic!
         * groovy -> kotlin dsl
         * shadowJar.archiveFile -> shadowJar.flatMap { it.archiveFile }
         * */
        inputFile.set(shadowJar.flatMap { it.archiveFile })
        dependsOn(shadowJar)
        /**
         * affect suffix of build jar name
         * if { archiveClassifier.set("forge") }
         * name will be examplemod-1.0.0-forge.jar
         */
        archiveClassifier.set("forge")
    }

    jar {
        archiveClassifier.set("dev")
    }

    sourcesJar {
        val commonSources = project(":common").tasks.getByName("sourcesJar", Jar::class)
        dependsOn(commonSources)
        from(commonSources.archiveFile.map { zipTree(it) })
    }


    publishing {
        publications {
            create<MavenPublication>("mavenForge") {
                artifactId = "${rootProject.property("archives_base_name")}-${project.name}"
                from(javaComponent)
            }
        }

        // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
        repositories {
            // Add repositories to publish to here.
        }
    }
}
