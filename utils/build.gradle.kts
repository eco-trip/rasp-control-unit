plugins {
    id("java-quality-common-convention")
}

group = "io.github.ecotrip"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)
    implementation(libs.org.json)
}

tasks.withType<Test> {
    useJUnitPlatform()
}