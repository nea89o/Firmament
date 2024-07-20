/*
 * SPDX-FileCopyrightText: 2024 Linnea Gräf <nea@nea.moe>
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
}

repositories {
    mavenCentral()
}
dependencies {
    ksp("dev.zacsweers.autoservice:auto-service-ksp:1.1.0")
    implementation("com.google.auto.service:auto-service-annotations:1.1.1")
    implementation("com.google.devtools.ksp:symbol-processing-api:1.9.23-1.0.20")
    implementation("com.google.code.gson:gson:2.11.0")
}
