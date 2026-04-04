// SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
//
// SPDX-License-Identifier: CC0-1.0
import org.jetbrains.kotlin.konan.properties.loadProperties

plugins {
	`kotlin-dsl`
	kotlin("jvm") version "2.0.21"
}

repositories {
	loadProperties(project.file("../gradle/repositories.properties").absolutePath)
		.forEach { (propName, propUrl) ->
			maven {
				this.name = propName as String
				this.url = uri(propUrl)
			}
		}
}

fun createPluginCoordinate(provider: Provider<PluginDependency>): String {
	val pluginDep = provider.get()
	val pluginId = pluginDep.pluginId
	val version = pluginDep.version
	return "${pluginId}:${pluginId}.gradle.plugin:${version}"
}

dependencies {
	implementation("com.github.romangraef:neaslicenseextractificator:1.1.0")
	api(createPluginCoordinate(libs.plugins.shadow))
	api(createPluginCoordinate(libs.plugins.loom))
	implementation("net.fabricmc:access-widener:2.1.0")
	implementation("com.google.code.gson:gson:2.10.1")
}
