plugins {
    application
}

group = "org.example"
version = "unspecified"

application {
    mainClass.set("Application")
}

repositories {
    maven {
        url = uri("https://maven.pkg.github.com/meboo/pi4j-v2-issue26")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
    mavenCentral()
}
dependencies {
    implementation(project(":domain"))
    implementation(project(":core"))
    implementation(project(":room-monitoring"))
    implementation("org.slf4j:slf4j-api:2.0.0-alpha1")
    implementation("org.slf4j:slf4j-simple:2.0.0-alpha1")
    implementation("io.github.meboo.pi4j:pi4j-core:2.2.0-issue26")
    implementation("io.github.meboo.pi4j:pi4j-plugin-raspberrypi:2.2.0-issue26")
    implementation("io.github.meboo.pi4j:pi4j-plugin-pigpio:2.2.0-issue26")
    implementation("io.github.meboo.pi4j:pi4j-plugin-linuxfs:2.2.0-issue26")
    implementation("com.jcraft:jsch:0.1.44-1")
    testImplementation("io.github.meboo.pi4j:pi4j-test:2.2.0-issue26")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}