pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        maven{
            url = uri("./plugin/build/maven-repo")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "BuildAAR"
include(":app")
include(":loginApi")
project(":loginApi").projectDir = File("./login/api")
include(":loginImpl")
project(":loginImpl").projectDir = File("./login/impl")
include(":accountApi")
project(":accountApi").projectDir = File("./account/api")
include(":accountImpl")
project(":accountImpl").projectDir = File("./account/impl")
include(":plugin")
