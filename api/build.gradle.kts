plugins {
    id("coco.kotlin-common")
    application
    kotlin("plugin.serialization")
}

application { mainClass.set("api.ApplicationKt") }

dependencies {
    implementation(project(":core"))
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.serialization.kotlinx.json)

    // Dev deps
    testImplementation(ktorLibs.server.testHost)
}

tasks.register<Jar>("fatJar") {
    archiveBaseName.set("api")
    manifest { attributes["Main-Class"] = "api.ApplicationKt" }
    from(sourceSets["main"].output)
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
