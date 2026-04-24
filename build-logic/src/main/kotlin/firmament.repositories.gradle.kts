import java.util.Properties

repositories {
	val props = Properties()
	rootProject.file("gradle/repositories.properties").reader()
		.use(props::load)
	props.forEach { (propName, propUrl) ->
		maven {
			this.name = propName as String
			this.url = uri(propUrl)
		}
	}
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
		//https://github.com/HotswapProjects/HotswapAgent/releases/download/RELEASE-2.0.3/hotswap-agent-2.0.3.jar
		patternLayout {
			artifact("RELEASE-[revision]/[artifact]-[revision].[ext]")
		}
		content {
			includeGroup("virtual.github.hotswapagent")
		}
		metadataSources {
			artifact()
		}
	}
	mavenLocal()
}
