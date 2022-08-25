plugins {
    `java-library`
    checkstyle
    jacoco
    id("com.github.spotbugs")
}

group = "io.github.ecotrip"

repositories {
    mavenCentral()
}

tasks.withType<Checkstyle>().configureEach {
    reports {
        xml.required.set(false)
        html.required.set(true)
        html.stylesheet = resources.text.fromFile("${rootDir}/config/xsl/checkstyle-custom.xsl")
    }
}

tasks {
    jacocoTestReport {
        reports {
            xml.required.set(false)
            csv.required.set(false)
            html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
        }
    }

    jacocoTestCoverageVerification {
        violationRules {
            rule { limit { minimum = BigDecimal.valueOf(0.0) } }
        }
    }

    check {
        dependsOn(jacocoTestCoverageVerification)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}