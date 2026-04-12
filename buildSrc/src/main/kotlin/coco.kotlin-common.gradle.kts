plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    // Use the Kotlin Test integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the JUnit 5 integration.
    testImplementation("org.junit.jupiter:junit-jupiter-engine:6.0.1")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

// Generate project-info.properties with the project root
tasks.named<ProcessResources>("processResources") {
    val projectRoot = rootProject.rootDir.absolutePath
    inputs.property("project.root", projectRoot)
    filesMatching("**/project-info.properties") {
        filter { line ->
            line.replace($$"${project.root}", projectRoot)
        }
    }
}