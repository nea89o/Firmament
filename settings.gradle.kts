/*
 * SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: CC0-1.0
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

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
	    maven {
		    url = uri("https://repo.nea.moe/releases")
	    }
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "Firmament"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include("symbols")
include("javaplugin")
include("testagent")
includeBuild("build-logic")
