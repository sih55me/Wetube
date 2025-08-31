include(":common", ":app", ":library", ":ffmpeg", ":aria2c", "frag-nav")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(uri("https://jitpack.io"))
        jcenter()
        maven (uri("https://plugins.gradle.org/m2/") )
    }
}

rootProject.name = "wetube"
include(":app")
 