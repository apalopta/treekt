// credits to https://dev.to/autonomousapps/tools-of-the-build-trade-the-making-of-a-tiny-kotlin-app-3eba

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.file.DuplicatesStrategy.EXCLUDE
import org.gradle.internal.jvm.Jvm
import proguard.gradle.ProGuardTask
import java.io.FilenameFilter

buildscript {
    // repositories {
//         mavenCentral()
//     }
    dependencies {
        // There is apparently no plugin
        classpath("com.guardsquare:proguard-gradle:7.1.0")
    }
}

plugins {
    id("de.apalopta.treekt.kotlin-application-conventions")

    id("com.github.johnrengelman.shadow") version ("7.0.0")
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

version = "0.3.2"
group = "de.apalopta.cmd"

application {
    // Define the main class for the application.
    mainClass.set("de.apalopta.treekt.MainKt")
}

tasks.jar {
    archiveFileName.set("${archiveBaseName.get()}-${project.version}-fat.jar")
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

val shadowJar = tasks.named<ShadowJar>("shadowJar") {
    outputs.upToDateWhen { false }
    group = "Build"
    description = "Creates a fat jar"
    archiveFileName.set("${archiveBaseName.get()}-${project.version}-all.jar")
    isReproducibleFileOrder = true

    from(sourceSets.main.get().output)
    from(project.configurations.runtimeClasspath)

    exclude("**/*.kotlin_metadata")
    exclude("**/*.kotlin_module")
    exclude("META-INF/maven/**")

    duplicatesStrategy = EXCLUDE
}

val minify = tasks.register<ProGuardTask>("minify") {
    configuration(file("proguard.pro"))

    injars(shadowJar.flatMap { it.archiveFile })
    outjars(layout.buildDirectory.file("libs/${project.name}-${version}.jar"))

    libraryjars(javaRuntime())
    libraryjars(mapOf("filter" to "!**META-INF/versions/**.class"), configurations.compileClasspath)
}

tasks.register<JavaExec>("runMin") {
    classpath = files(minify).from(minify)
}

val startShadowScripts = tasks.named<CreateStartScripts>("startShadowScripts") {
    classpath = files(minify)
}

val minifiedDistZip = tasks.register<Zip>("minifiedDistZip") {
//    archiveClassifier.set("minified")

    val zipRoot = "/${project.name}-${project.version}"
    from(minify) {
        into("$zipRoot/lib")
    }
    from(startShadowScripts) {
        into("$zipRoot/bin")
    }
}

val minifiedDistTar = tasks.register<Tar>("minifiedDistTar") {
//    archiveClassifier.set("minified")

    val tarRoot = "/${project.name}-${project.version}"
    from(minify) {
        into("$tarRoot/lib")
    }
    from(startShadowScripts) {
        into("$tarRoot/bin")
    }
}

// tweak the standard dist's output directory
tasks.named<Sync>("installDist") {
    destinationDir = layout.buildDirectory.file("install/treekt-fat").get().asFile
}
// the minified output will be the standard installation
tasks.named<Sync>("installShadowDist") {
    destinationDir = layout.buildDirectory.file("install/treekt").get().asFile
}

tasks.register("install") {
    group = "Distribution"
    description = "Installs an optimized version distribution."
    dependsOn(tasks.named("installShadowDist"))
}

class MyFilenameFilter : FilenameFilter {
    override fun accept(file: File, name: String) = name.endsWith(".jar") || name.endsWith(".jmod")
}

/**
 * @return The JDK runtime, for use by Proguard.
 */
fun javaRuntime(): List<File> {
    val jvm = Jvm.current()
    val fileFilter = MyFilenameFilter()

    return listOf("jmods" /* JDK 9+ */, "bundle/Classes" /* mac */, "jre/lib" /* linux */)
        .map { File(jvm.javaHome, it) }
        .filter { it.exists() }
        .flatMap { it.listFiles(fileFilter)!!.toList() }.sorted()
        .also {
            if (it.isEmpty()) {
                throw IllegalStateException("Could not find JDK ${jvm.javaVersion!!.majorVersion} runtime")
            }
        }
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
        // don't publish the huge standard files
//        create<MavenPublication>("treekt") {
//            from(components["java"])
//            artifact(tasks.distZip)
//            artifact(tasks.distTar)
//        }
        create<MavenPublication>("treektMinified") {
            artifact(minify)
            artifact(minifiedDistZip)
            artifact(minifiedDistTar)
        }
    }
}
