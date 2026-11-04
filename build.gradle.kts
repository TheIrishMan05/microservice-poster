plugins {
    java
    id("org.springframework.boot") version "4.0.9" apply false
    jacoco
}

allprojects {
    group = "ifmo.poster"

    repositories {
        mavenCentral()
    }
}

val coverageModules = setOf("user-service", "event-service", "order-service")

subprojects {
    apply(plugin = "java")
    apply(plugin = "jacoco")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    dependencies {
        implementation(platform("org.springframework.boot:spring-boot-dependencies:4.0.9"))
        implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2025.1.4"))
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        finalizedBy(tasks.jacocoTestReport)
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    if (name in coverageModules) {
        tasks.jacocoTestCoverageVerification {
            dependsOn(tasks.jacocoTestReport)
            violationRules {
                rule {
                    limit {
                        counter = "LINE"
                        value = "COVEREDRATIO"
                        minimum = "0.70".toBigDecimal()
                    }
                }
            }
            classDirectories.setFrom(
                files(classDirectories.files.map {
                    fileTree(it) {
                        exclude(
                            "**/*Application*",
                            "**/config/**",
                            "**/OpenApiConfig*"
                        )
                    }
                })
            )
        }

        tasks.named("check") {
            dependsOn(tasks.jacocoTestCoverageVerification)
        }
    }
}
