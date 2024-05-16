pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
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
        maven(url = uri("https://jitpack.io"))
        jcenter()  // 이전에 jcenter() 리포지터리가 포함되어 있었으나 이제는 사용하지 않는 것을 권장합니다.
        // 필요한 경우만 포함하시고, 가능하다면 대체 리포지터리를 사용하세요.
    }
}

rootProject.name = "epilog"
include(":app")
include(":wear")
