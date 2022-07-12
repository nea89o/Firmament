plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    maven { url = uri("https://maven.quiltmc.org/repository/release/") }
    mavenCentral()
}

architectury {
    platformSetupLoomIde()
    loader("quilt")
}

loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)
}

/**
 * @see: https://docs.gradle.org/current/userguide/migrating_from_groovy_to_kotlin_dsl.html
 * */
val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating // Don't use shadow from the shadow plugin because we don't want IDEA to index this.

val developmentQuilt: Configuration = configurations.getByName("developmentQuilt")
configurations {
    compileClasspath.get().extendsFrom(configurations["common"])
    runtimeClasspath.get().extendsFrom(configurations["common"])
    developmentQuilt.extendsFrom(configurations["common"])
}

dependencies {
    modImplementation("org.quiltmc:quilt-loader:${rootProject.property("quilt_loader_version")}")
    modApi("org.quiltmc.quilted-fabric-api:quilted-fabric-api:${rootProject.property("quilt_fabric_api_version")}")
    // Remove the next few lines if you don't want to depend on the API
    modApi("dev.architectury:architectury-fabric:${rootProject.property("architectury_version")}") {
        // We must not pull Fabric Loader from Architectury Fabric
        exclude(group = "net.fabricmc")
        exclude(group = "net.fabricmc.fabric-api")
    }

    common(project(":common", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(":common", configuration = "transformProductionQuilt")) { isTransitive = false }
    common(kotlin("stdlib-jdk8"))
}

val javaComponent = components.getByName("java", AdhocComponentWithVariants::class)
javaComponent.withVariantsFromConfiguration(configurations["sourcesElements"]) {
    skip()
}

tasks {
    processResources {
        inputs.property("group", rootProject.property("maven_group"))
        inputs.property("version", project.version)

        filesMatching("quilt.mod.json") {
            expand(
                    "group" to rootProject.property("maven_group"),
                    "version" to project.version
            )
        }
    }

    shadowJar {
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
        injectAccessWidener.set(true)
        /**
         * magic!
         * groovy -> kotlin dsl
         * shadowJar.archiveFile -> shadowJar.flatMap { it.archiveFile }
         * */
        inputFile.set(shadowJar.flatMap { it.archiveFile })
        dependsOn(shadowJar)
        /**
         * affect suffix of build jar name
         * if { archiveClassifier.set("quilt") }
         * name will be examplemod-1.0.0-quilt.jar
         */
        archiveClassifier.set("quilt")
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
            create<MavenPublication>("mavenQuilt") {
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