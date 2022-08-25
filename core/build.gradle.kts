plugins {
    id("java-quality-common-convention")
}

group = "io.github.ecotrip"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)
}

tasks.withType<Test> {
    useJUnitPlatform()
}