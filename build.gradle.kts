import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.AbstractRemapJarTask

plugins {
	id("java")
	id("fabric-loom") version("1.4.+")
	id("com.github.johnrengelman.shadow") version("8.0.0+")
//	id("me.hypherionmc.cursegradle") version("2.+")
}

repositories {
	mavenCentral()
	exclusiveContent {
		forRepository {
			maven("https://maven.fabricmc.net/")
		}
		filter {
			includeGroupByRegex("net\\.fabricmc.*")
			includeGroup("fabric-loom")
		}
	}
	exclusiveContent {
		forRepository {maven("https://masa.dy.fi/maven")
		}
		filter {
			includeGroup("carpet")
		}
	}
}

val modId: String by project
val modName: String by project
val modAuthor: String by project
val modVersion: String by project
val modDescription: String by project
val modUrl: String by project
val minecraftVersion: String by project
val javaVersion: String by project
val javaToolchainVersion: String by project
val jarName: String by project
val compatibleMinecraftVersions: String by project
val curseProjectId: String by project

val fabricLoaderVersion: String by project
val bigMathVersion: String by project
val commonsCompressVersion: String by project
val carpetVersion: String by project

version = "$minecraftVersion-$modVersion"

base {
	archivesName.set(jarName)
}

java {
	sourceCompatibility = JavaVersion.toVersion(javaVersion)
	targetCompatibility = JavaVersion.toVersion(javaVersion)
	toolchain.languageVersion = JavaLanguageVersion.of(javaToolchainVersion)
}

val shadowImplementation: Configuration by configurations.creating

configurations["shadowImplementation"].extendsFrom(configurations["implementation"])
configurations["compileClasspath"].extendsFrom(shadowImplementation)

dependencies {
	minecraft("com.mojang:minecraft:${minecraftVersion}")
	mappings(loom.officialMojangMappings())
	modImplementation("net.fabricmc:fabric-loader:${fabricLoaderVersion}")
	modImplementation("carpet:fabric-carpet:${carpetVersion}")
	shadowImplementation("ch.obermuhlner:big-math:${bigMathVersion}")
	shadowImplementation("org.apache.commons:commons-compress:${commonsCompressVersion}")
}

loom {
	accessWidenerPath = file("src/main/resources/kardexotools.accesswidener")
	@Suppress("UnstableApiUsage")
	mixin.defaultRefmapName = "$modId.refmap.json"
}

tasks.named<ProcessResources>("processResources").configure {
	val properties = mapOf(
		"modVersion" to modVersion,
		"modId" to modId,
		"modName" to modName,
		"modAuthor" to modAuthor,
		"modDescription" to modDescription,
		"modUrl" to modUrl,
		"minecraftVersion" to minecraftVersion
	)
	
	inputs.properties(properties)
	
	filesMatching(listOf("fabric.mod.json")) {
		expand(properties)
	}
}

val shadowJarTask = tasks.named<ShadowJar>("shadowJar") {
	configurations = listOf(shadowImplementation)
	from(sourceSets.main.get().output)
	duplicatesStrategy = DuplicatesStrategy.INCLUDE
	archiveClassifier = "dev"
	dependsOn(tasks.named("relocateShadowJar"))
}

tasks.register<ConfigureShadowRelocation>("relocateShadowJar").configure {
	target = shadowJarTask.get()
	prefix = "net.kardexo.kardexotools.include"
}

tasks.named<AbstractRemapJarTask>("remapJar").configure {
	dependsOn(shadowJarTask)
	inputFile.set(shadowJarTask.get().archiveFile)
}

tasks.withType<JavaCompile>().configureEach {
	options.encoding = "UTF-8"
	options.release.set(JavaLanguageVersion.of(javaVersion).asInt())
}

tasks.withType<Javadoc> {
	with(options as StandardJavadocDocletOptions) {
		addStringOption("Xdoclint:none", "-quiet")
	}
}

tasks.withType<AbstractArchiveTask>().configureEach {
	isPreserveFileTimestamps = false
	isReproducibleFileOrder = true
}

//curseforge {
//	apiKey = project.hasProperty("curse_api_key") ? curse_api_key : ''
//	project {
//		id = curse_project_id
//		changelog = file('./changelog.txt').canRead() ? file('./changelog.txt').text : ''
//		changelogType = 'text'
//		releaseType = 'release'
//		addGameVersion 'Fabric'
//		compatible_minecraft_versions.split(",").each {
//			addGameVersion(it)
//		}
//		mainArtifact(remapJar) {
//			displayName = "${mod_name}-${minecraft_version}-${mod_version}"
//		}
//	}
//	options {
//		javaVersionAutoDetect = false
//		forgeGradleIntegration = false
//	}
//}
