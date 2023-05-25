pluginManagement {
    repositories {
        mavenLocal()
        maven {
            name = "fabricmc"
            url = uri("https://maven.fabricmc.net/")
        }
        maven {
            name = "architectury"
            url = uri("https://maven.architectury.dev/")
        }
        maven {
            name = "forgemc"
            url = uri("https://maven.minecraftforge.net/")
        }
        maven {
            name = "jitpack"
            url = uri("https://jitpack.io")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "Firmament"

