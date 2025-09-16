include(":app")

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

        maven("https://maven.mozilla.org/maven2/")

        maven(uri("https://jitpack.io"))
        jcenter()
        maven (uri("https://plugins.gradle.org/m2/") )
    }
}

rootProject.name = "wetube"
include(":app")
 