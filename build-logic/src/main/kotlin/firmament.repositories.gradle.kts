repositories {
	mavenCentral()
	maven("https://maven.terraformersmc.com/releases/")
	maven("https://maven.shedaniel.me")
	maven("https://maven.fabricmc.net")
	maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
	maven("https://api.modrinth.com/maven") {
		content {
			includeGroup("maven.modrinth")
		}
	}
	maven("https://repo.sleeping.town") {
		content {
			includeGroup("com.unascribed")
		}
	}
	ivy("https://github.com/HotswapProjects/HotswapAgent/releases/download") {
		patternLayout {
			artifact("[revision]/[artifact]-[revision].[ext]")
		}
		content {
			includeGroup("virtual.github.hotswapagent")
		}
		metadataSources {
			artifact()
		}
	}
	maven("https://server.bbkr.space/artifactory/libs-release")
	maven("https://repo.nea.moe/releases")
	maven("https://maven.notenoughupdates.org/releases")
	maven("https://repo.nea.moe/mirror")
	maven("https://jitpack.io/") {
		content {
			includeGroupByRegex("(com|io)\\.github\\..+")
			excludeModule("io.github.cottonmc", "LibGui")
		}
	}
	maven("https://repo.hypixel.net/repository/Hypixel/")
	maven("https://maven.azureaaron.net/snapshots")
	maven("https://maven.azureaaron.net/releases")
	maven("https://www.cursemaven.com")
	maven("https://maven.isxander.dev/releases") {
		name = "Xander Maven"
	}
	mavenLocal()
}
