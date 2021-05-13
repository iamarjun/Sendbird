dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://repo.sendbird.com/public/maven")
    }
}
rootProject.name = "Sendbird"
include(":app")
include(":media")
