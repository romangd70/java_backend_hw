rootProject.name = "Seminar8"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven(url = "https://artifactory.tcsbank.ru/artifactory/maven-all")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven(url = "https://artifactory.tcsbank.ru/artifactory/maven-all")
    }
}

buildscript {
    repositories {
        mavenCentral()
        maven(url = "https://artifactory.tcsbank.ru/artifactory/maven-all")
    }
}
