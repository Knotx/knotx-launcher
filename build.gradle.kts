/*
 * Copyright (C) 2019 Knot.x Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Date
import org.apache.tools.ant.filters.ReplaceTokens
import org.nosphere.apache.rat.RatTask

repositories {
    mavenLocal()
    maven { url = uri("https://plugins.gradle.org/m2/") }
    maven { url = uri("http://repo1.maven.org/maven2") }
    maven { url = uri("https://oss.sonatype.org/content/groups/staging/") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
}

plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
    id("org.nosphere.apache.rat") version "0.4.0"
}

group = "io.knotx"

// -----------------------------------------------------------------------------
// Dependencies
// -----------------------------------------------------------------------------
val junitTestCompile = configurations.create("junitTestCompile")

tasks.named<JavaCompile>("compileJava") {
    options.annotationProcessorGeneratedSourcesDirectory = file("src/main/generated")
}

tasks.named<Delete>("clean") {
    delete.add("src/main/generated")
}

dependencies {
    implementation(platform("io.knotx:knotx-dependencies:${project.version}"))
    implementation(group = "io.vertx", name = "vertx-core")
    implementation(group = "io.vertx", name = "vertx-rx-java2")
    implementation(group = "io.vertx", name = "vertx-config")
    implementation(group = "io.vertx", name = "vertx-config-hocon")

    implementation(group = "org.apache.commons", name = "commons-lang3")
    implementation(group = "com.google.guava", name = "guava")

    testImplementation(group = "io.knotx", name = "knotx-junit5")
    testImplementation(group = "io.vertx", name = "vertx-junit5")
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api")
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-params")
    testImplementation(group = "io.vertx", name = "vertx-unit")
}

junitTestCompile.extendsFrom(configurations.named("testImplementation").get())
// -----------------------------------------------------------------------------
// Source sets
// -----------------------------------------------------------------------------

tasks.withType<JavaCompile>().configureEach {
    with(options) {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
        encoding = "UTF-8"
    }
}

sourceSets.named("main") {
    java.srcDir("src/main/generated")
}
sourceSets.create("junitTest") {
    compileClasspath += sourceSets.named("main").get().output
}

// -----------------------------------------------------------------------------
// Tasks
// -----------------------------------------------------------------------------


tasks {
    //FIXME there is race condition with copying Version to generated and compiling project
    register<Copy>("templatesProcessing") {
      val now = Date().time
      val tokens = mapOf("project.version" to project.version, "build.timestamp" to "${now}")
      inputs.properties(tokens)

      from("src/main/java-templates") {
        include("*.java")
        filter<ReplaceTokens>("tokens" to tokens)
      }
      into("src/main/generated/io/knotx/launcher")
    }
    getByName<JavaCompile>("compileJava").dependsOn("templatesProcessing")

    named<RatTask>("rat") {
        excludes.addAll("*.yml", "*.md", "**/*.md", "*.properties","**/build/*", "**/out/*", "**/generated/*", "gradle/wrapper/*", "gradlew", "gradlew.bat", ".idea/*")
    }
    getByName("build").dependsOn("rat")

    named<Test>("test") {
        useJUnitPlatform()
        testLogging { showStandardStreams = true }
        testLogging { showExceptions = true }
        failFast = true
    }
}

// -----------------------------------------------------------------------------
// Publication
// -----------------------------------------------------------------------------
tasks.register<Jar>("sourcesJar") {
    from(sourceSets.named("main").get().allJava)
    classifier = "sources"
}

tasks.register<Jar>("javadocJar") {
    from(tasks.named<Javadoc>("javadoc"))
    classifier = "javadoc"
}

tasks.register<Jar>("testJar") {
    from(sourceSets.named("junitTest").get().output)
    classifier = "tests"
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "knotx-launcher"
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            artifact(tasks["testJar"])
            pom {
                name.set("Knot.x Launcher")
                description.set("Knot.x Launcher - deploys all Knot.x modules in Vert.x instance.")
                url.set("http://knotx.io")
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("marcinczeczko")
                        name.set("Marcin Czeczko")
                        email.set("https://github.com/marcinczeczko")
                    }
                    developer {
                        id.set("skejven")
                        name.set("Maciej Laskowski")
                        email.set("https://github.com/Skejven")
                    }
                    developer {
                        id.set("tomaszmichalak")
                        name.set("Tomasz Michalak")
                        email.set("https://github.com/tomaszmichalak")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/Knotx/knotx-launcher.git")
                    developerConnection.set("scm:git:ssh://github.com:Knotx/knotx-launcher.git")
                    url.set("http://knotx.io")
                }
            }
        }
        repositories {
            maven {
                val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
                val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots"
                url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
                credentials {
                    username = if (project.hasProperty("ossrhUsername")) project.property("ossrhUsername")?.toString() else "UNKNOWN"
                    password = if (project.hasProperty("ossrhPassword")) project.property("ossrhPassword")?.toString() else "UNKNOWN"
                    println("Connecting with user: ${username}")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

tasks.named<Javadoc>("javadoc") {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}
