@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        exclusiveContent {
            forRepository {
                maven("https://maven.fabricmc.net/")
            }
            filter {
                includeGroupAndSubgroups("net.fabricmc")
                includeGroup("fabric-loom")
            }
        }
        gradlePluginPortal()
    }
}

rootProject.name = "KardExoTools"
