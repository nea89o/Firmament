plugins {
    kotlin("jvm") version "1.8.10"
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
}

sourceSets {
    main {
        kotlin {
            srcDir(file("src"))
        }
    }
}
