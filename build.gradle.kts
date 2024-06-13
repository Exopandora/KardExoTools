@file:Suppress("UnstableApiUsage")
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.AbstractRemapJarTask
import net.fabricmc.loom.task.RemapJarTask

plugins {
	id("java")
	alias(libs.plugins.fabricloom)
	alias(libs.plugins.shadow)
	alias(libs.plugins.modpublishplugin)
}

repositories {
	mavenCentral()
	exclusiveContent {
		forRepository {
			maven("https://maven.fabricmc.net/")
		}
		filter {
			includeGroupAndSubgroups("net.fabricmc")
			includeGroup("fabric-loom")
		}
	}
	exclusiveContent {
		forRepository {
			maven("https://masa.dy.fi/maven")
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
val javaVersion: String by project
val javaToolchainVersion: String by project
val jarName: String by project
val compatibleMinecraftVersions: String by project
val curseProjectId: String by project

version = "${libs.versions.minecraft.get()}-$modVersion"

base {
	archivesName.set(jarName)
}

java {
	sourceCompatibility = JavaVersion.toVersion(javaVersion)
	targetCompatibility = JavaVersion.toVersion(javaVersion)
	toolchain.languageVersion = JavaLanguageVersion.of(javaToolchainVersion)
}

idea {
	module {
		isDownloadSources = true
		isDownloadJavadoc = true
	}
}

val shadowImplementation: Configuration by configurations.creating

configurations["shadowImplementation"].extendsFrom(configurations["implementation"])
configurations["compileClasspath"].extendsFrom(shadowImplementation)

dependencies {
	minecraft(libs.minecraft.fabric)
	mappings(loom.officialMojangMappings())
	modImplementation(libs.fabric.loader)
	modImplementation(libs.carpet.fabric)
	shadowImplementation(libs.bigmath)
	shadowImplementation(libs.apache.commons.compress)
}

loom {
	accessWidenerPath = file("src/main/resources/kardexotools.accesswidener")
	mixin.defaultRefmapName = "$modId.refmap.json"
}

tasks.named<ProcessResources>("processResources") {
	val properties = mapOf(
		"modVersion" to modVersion,
		"modId" to modId,
		"modName" to modName,
		"modAuthor" to modAuthor,
		"modDescription" to modDescription,
		"modUrl" to modUrl,
		"minecraftVersion" to libs.versions.minecraft.get()
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
	isEnableRelocation = true
	relocationPrefix = "net.kardexo.kardexotools.include"
}

tasks.named<AbstractRemapJarTask>("remapJar") {
	dependsOn(shadowJarTask)
	inputFile.set(shadowJarTask.get().archiveFile)
}

tasks.withType<JavaCompile> {
	options.encoding = "UTF-8"
	options.release.set(JavaLanguageVersion.of(javaVersion).asInt())
}

tasks.withType<Javadoc> {
	with(options as StandardJavadocDocletOptions) {
		addStringOption("Xdoclint:none", "-quiet")
	}
}

tasks.withType<AbstractArchiveTask> {
	isPreserveFileTimestamps = false
	isReproducibleFileOrder = true
}

publishMods {
	displayName = "$jarName-${libs.versions.minecraft.get()}-$modVersion"
	file = tasks.named<RemapJarTask>("remapJar").get().archiveFile
	changelog = provider { file("changelog.txt").readText() }
	modLoaders.add("fabric")
	type = STABLE
	
	val compatibleVersions = compatibleMinecraftVersions.split(",")
	
	curseforge {
		projectId = curseProjectId
		accessToken = findProperty("curse_api_key").toString()
		minecraftVersions.set(compatibleVersions)
		javaVersions.add(JavaVersion.toVersion(javaVersion))
		clientRequired = false
		serverRequired = true
	}
}
