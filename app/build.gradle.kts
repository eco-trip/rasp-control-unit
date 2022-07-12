plugins {
    application
}

java {
    modularity.inferModulePath.set(true)
}

application {
    mainClass.set("com.pi4j.example.MinimalExample")
}

tasks.register<Copy>("copyDistribution") {
    from(configurations.default)
    from(tasks.named("jar"))
    from(layout.projectDirectory.file("assets/run.sh"))
    into(layout.buildDirectory.dir("distributions"))
}


tasks.named("build") {
    dependsOn("copyDistribution")
}

// Dependencies for this application, until V2 is released, snapshot is required here
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
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("org.slf4j:slf4j-simple:1.7.32")
    implementation("io.github.meboo.pi4j:pi4j-core:2.2.0-issue26")
    implementation("io.github.meboo.pi4j:pi4j-plugin-raspberrypi:2.2.0-issue26")
    implementation("io.github.meboo.pi4j:pi4j-plugin-pigpio:2.2.0-issue26")
    // api fileTree(dir: 'lib', includes: ['*.jar'])
    implementation("com.jcraft:jsch:0.1.44-1")
    // implementation(fileTree("libs"))
    /*
    implementation files('libs/pi4j-core.jar')
    implementation files('libs/pi4j-core-javadocs.jar')
    implementation files('libs/pi4j-core-sources.jar')
    implementation files('libs/pi4j-library-pigpio.jar')
    implementation files('libs/pi4j-plugin-pigpio.jar')
    implementation files('libs/pi4j-plugin-raspberrypi.jar')
    implementation files('libs/pi4j-plugin-linuxfs.jar')
    implementation files('libs/slf4j-api.jar')
    implementation files('libs/slf4j-simple.jar')
    */
}
