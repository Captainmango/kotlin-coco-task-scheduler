plugins {
    id("coco.kotlin-common")
    application
}

application { mainClass.set("api.MainKt") }

dependencies {
    implementation(project(":core"))
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.netty)
}

tasks.register<Jar>("fatJar") {
    archiveBaseName.set("api")
    manifest { attributes["Main-Class"] = "api.MainKt" }
    from(sourceSets["main"].output)
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
