@file:Suppress("UnstableApiUsage")
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	id("java")
	id("idea")
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

val modId: String = project.property("modId")!!.toString()
val modName: String = project.property("modName")!!.toString()
val modAuthor: String = project.property("modAuthor")!!.toString()
val modVersion: String = project.property("modVersion")!!.toString()
val modDescription: String = project.property("modDescription")!!.toString()
val modUrl: String = project.property("modUrl")!!.toString()
val javaVersion: String = project.property("javaVersion")!!.toString()
val javaToolchainVersion: String = project.property("javaToolchainVersion")!!.toString()
val jarName: String = project.property("jarName")!!.toString()
val compatibleMinecraftVersions: String = project.property("compatibleMinecraftVersions")!!.toString()
val curseProjectId: String = project.property("curseProjectId")!!.toString()

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

val shadowImplementation: Configuration = configurations.create("shadowImplementation")

configurations["compileClasspath"].extendsFrom(shadowImplementation)
configurations["runtimeClasspath"].extendsFrom(shadowImplementation)

dependencies {
	minecraft(libs.minecraft.fabric)
	implementation(libs.fabric.loader)
	implementation(libs.carpet.fabric)
	shadowImplementation(libs.bigmath)
	shadowImplementation(libs.apache.commons.compress)
}

loom {
	accessWidenerPath = file("src/main/resources/kardexotools.accesswidener")
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
	archiveClassifier = ""
	enableAutoRelocation = true
	relocationPrefix = "net.kardexo.kardexotools.include"
}

tasks.named<Jar>("jar") {
	archiveClassifier = "dev"
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
	file = tasks.named<ShadowJar>("shadowJar").get().archiveFile
	changelog = provider { file("changelog.txt").readText() }
	modLoaders.add("fabric")
	type = STABLE
	
	val compatibleVersions = compatibleMinecraftVersions.split(",")
	
	curseforge {
		projectId = curseProjectId
		accessToken = findProperty("curse_api_key").toString()
		minecraftVersions.set(compatibleVersions)
		javaVersions.add(JavaVersion.toVersion(javaVersion))
		client = false
		server = true
	}
}
