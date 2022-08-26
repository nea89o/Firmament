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
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "moe.nea.licenseextractificator" -> useModule("com.github.romangraef:neaslicenseextractificator:${requested.version}")
            }
        }
    }
}

rootProject.name = "NotEnoughApdates"

