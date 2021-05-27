import org.gradle.api.file.DuplicatesStrategy.*

/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    id("de.apalopta.treekt.kotlin-application-conventions")
    `maven-publish`
}

dependencies {
//    implementation("org.apache.commons:commons-text")
//    implementation(project(":utilities"))
    implementation("org.jetbrains.kotlinx:kotlinx-cli-jvm:0.3.2")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

version = "0.1.0"

application {
    // Define the main class for the application.
    mainClass.set("de.apalopta.treekt.MainKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "de.apalopta.treekt.MainKt"
    }
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })

    duplicatesStrategy = EXCLUDE
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/apalopta/treekt")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("gpr") {
            groupId = "de.apalopta.cmd"
            artifactId = "treekt"
            from(components["java"])
        }
    }
}
