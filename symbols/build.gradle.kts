plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
	id("firmament.common")
}

dependencies {
    ksp("dev.zacsweers.autoservice:auto-service-ksp:1.1.0")
    implementation("com.google.auto.service:auto-service-annotations:1.1.1")
    implementation("com.google.devtools.ksp:symbol-processing-api:1.9.23-1.0.20")
    implementation("com.google.code.gson:gson:2.11.0")
}
