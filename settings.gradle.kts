pluginManagement {
    repositories {
        exclusiveContent {
            forRepository {
                maven("https://maven.fabricmc.net/")
            }
            filter {
                includeGroup("net.fabricmc")
                includeGroup("fabric-loom")
            }
        }
        gradlePluginPortal()
    }
}

rootProject.name = "KardExoTools"
