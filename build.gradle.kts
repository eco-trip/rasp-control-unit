// https://github.com/KengoTODA/gradle-semantic-release-plugin/issues/435
tasks.register<DefaultTask>("publish") {
    group = "publish"
    description = "Dummy publish to pass the verification phase of the gradle-semantic-release-plugin"
}