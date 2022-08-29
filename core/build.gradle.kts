plugins {
    id("java-quality-common-convention")
}

group = "io.github.ecotrip"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":utils"))
    implementation(project(":domain"))
    testImplementation(libs.mockito)
    testImplementation(libs.junit.jupiter)
}

tasks.withType<Test> {
    useJUnitPlatform()
}