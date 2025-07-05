// SPDX-FileCopyrightText: 2023 Linnea Gräf <nea@nea.moe>
//
// SPDX-License-Identifier: CC0-1.0

plugins {
	kotlin("jvm") version "2.1.0"
	`kotlin-dsl`
}
repositories {
	mavenCentral()
	maven {
		name = "jitpack"
		url = uri("https://jitpack.io")
	}
}
dependencies {
	implementation("com.github.romangraef:neaslicenseextractificator:1.1.0")
	api("com.gradleup.shadow:shadow-gradle-plugin:9.0.0-rc1")
	implementation("net.fabricmc:access-widener:2.1.0")
	implementation("com.google.code.gson:gson:2.10.1")
}

sourceSets {
	main {
		kotlin {
			srcDir(file("src"))
		}
	}
}
