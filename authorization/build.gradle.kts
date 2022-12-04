plugins {
    id("java-quality-common-convention")
}

group = "io.github.ecotrip"

repositories {
    maven {
        url = uri("https://maven.pkg.github.com/meboo/pi4j-v2-issue26")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GH_PACKAGES_USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GH_PACKAGES_TOKEN")
        }
    }
    mavenCentral()
}

dependencies {
    implementation(project(":utils"))
    implementation(project(":domain"))
    implementation(project(":core"))
//    implementation(libs.jsch)
//    implementation(libs.bundles.slf4j)
    implementation(libs.bundles.pi4j)
    testImplementation(libs.pi4j.test)
    testImplementation(libs.mockito)
    testImplementation(libs.junit.jupiter)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

spotbugs {
    excludeFilter.set(file("config/spotbugs-exclude.xml"));
}
