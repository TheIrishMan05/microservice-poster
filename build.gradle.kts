plugins {
    java
    id("org.springframework.boot") version "4.0.9" apply false
}

allprojects {
    group = "ifmo.poster"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    
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
    }
}