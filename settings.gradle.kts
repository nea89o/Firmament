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
		val props = java.util.Properties()
		file("gradle/repositories.properties").reader()
			.use(props::load)
		props.forEach { (propName, propUrl) ->
			maven {
				this.name = propName as String
				this.url = uri(propUrl)
			}
		}
	}
}

rootProject.name = "Firmament"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include("symbols")
include("javaplugin")
include("testagent")
includeBuild("build-logic")
