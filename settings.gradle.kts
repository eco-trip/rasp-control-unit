rootProject.name = "rasp-control-unit"

include("utils")
include("domain")
include("core")
include("room-monitoring")
include("authorization")
include("examples")

plugins {
    id("com.gradle.enterprise") version "3.11.1"
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "1.0.19"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishOnFailure()
    }
}

gitHooks {
    preCommit {
        tasks("check")
    }
    commitMsg { conventionalCommits() }
    createHooks(true)
}
