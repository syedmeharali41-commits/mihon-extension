pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
        maven { url = java.net.URI("https://jitpack.io") }
    }
}

rootProject.name = "mihon-ybxmanga-extension"

include(":core")
include(":src:all:ybxmanga")
