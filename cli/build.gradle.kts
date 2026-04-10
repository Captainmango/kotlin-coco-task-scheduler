plugins {
    id("coco.kotlin-common")
    application
}

application {
    mainClass.set("coco.cli.MainKt")
}

dependencies {
    implementation(project(":core"))
    implementation(libs.clikt)
}

tasks.register<Jar>("fatJar") {
    archiveBaseName.set("cli")
    manifest { attributes["Main-Class"] = "coco.cli.MainKt" }
    from(sourceSets["main"].output)
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) } )
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}