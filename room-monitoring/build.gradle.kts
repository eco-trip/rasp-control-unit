plugins {
    id("java")
}

group = "io.github.ecotrip"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}