// settings.gradle.kts

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
    }
}

rootProject.name = "PandoraOS"
include(
    ":app",
    ":core-ui",
    ":core-data",
    ":core-cac",
    ":core-ai",
    ":feature-overlay",
    ":feature-keyboard"
    // Các module provider-llm và feature-flow sẽ được thêm sau
)
