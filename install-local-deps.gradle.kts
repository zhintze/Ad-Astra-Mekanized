/**
 * Gradle script to install JAR dependencies to local Maven repository
 *
 * Run with: ./gradlew -b install-local-deps.gradle.kts installLocalDeps
 */

plugins {
    id("maven-publish")
}

// Define local JAR files and their Maven coordinates
val localJars = mapOf(
    "libs/Mekanism-1.21.1-10.7.14.homebaked.jar" to Triple("dev.local", "mekanism", "10.7.14"),
    "libs/MekanismGenerators-1.21.1-10.7.14.homebaked.jar" to Triple("dev.local", "mekanism-generators", "10.7.14"),
    "libs/MekanismAdditions-1.21.1-10.7.14.homebaked.jar" to Triple("dev.local", "mekanism-additions", "10.7.14"),
    "libs/MekanismTools-1.21.1-10.7.14.homebaked.jar" to Triple("dev.local", "mekanism-tools", "10.7.14"),
    "libs/ImmersiveEngineering-1.21.1-12.4.3-194.jar" to Triple("dev.local", "immersive-engineering", "12.4.3")
)

tasks.register("installLocalDeps") {
    group = "setup"
    description = "Install local JAR dependencies to Maven local repository"

    doLast {
        localJars.forEach { (jarPath, coords) ->
            val (groupId, artifactId, version) = coords
            val jarFile = file(jarPath)

            if (!jarFile.exists()) {
                println("WARNING: JAR file not found: $jarPath")
                return@forEach
            }

            println("Installing $jarPath as $groupId:$artifactId:$version")

            // Use Gradle's built-in Maven publish to install to local repo
            exec {
                commandLine(
                    "gradle",
                    "publishToMavenLocal",
                    "-Pjar.file=${jarFile.absolutePath}",
                    "-Pjar.groupId=$groupId",
                    "-Pjar.artifactId=$artifactId",
                    "-Pjar.version=$version"
                )
                isIgnoreExitValue = true
            }
        }

        println("Local dependencies installation complete!")
        println("Add 'mavenLocal()' to your repositories block in build.gradle.kts")
    }
}

// Alternative: Create a simple copy task that organizes JARs in Maven structure
tasks.register("setupLocalMaven") {
    group = "setup"
    description = "Set up local Maven-style repository structure"

    doLast {
        val localRepo = file("${System.getProperty("user.home")}/.m2/repository")

        localJars.forEach { (jarPath, coords) ->
            val (groupId, artifactId, version) = coords
            val jarFile = file(jarPath)

            if (!jarFile.exists()) {
                println("WARNING: JAR file not found: $jarPath")
                return@forEach
            }

            // Create Maven directory structure
            val groupPath = groupId.replace(".", "/")
            val targetDir = file("$localRepo/$groupPath/$artifactId/$version")
            targetDir.mkdirs()

            // Copy JAR file
            val targetJar = file("$targetDir/$artifactId-$version.jar")
            jarFile.copyTo(targetJar, overwrite = true)

            // Create minimal POM file
            val pomContent = """<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>$groupId</groupId>
    <artifactId>$artifactId</artifactId>
    <version>$version</version>
    <packaging>jar</packaging>
    <description>Local installation of $artifactId</description>
</project>"""

            val pomFile = file("$targetDir/$artifactId-$version.pom")
            pomFile.writeText(pomContent)

            println("Installed: $groupId:$artifactId:$version")
        }

        println("\nLocal Maven repository setup complete!")
        println("Repository location: $localRepo")
        println("\nAdd these dependencies to your build.gradle.kts:")
        localJars.values.forEach { (groupId, artifactId, version) ->
            println("  implementation(\"$groupId:$artifactId:$version\")")
        }
    }
}