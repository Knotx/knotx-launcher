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
import org.nosphere.apache.rat.RatTask

group = "io.knotx"
defaultTasks("distZip")

plugins {
    id("io.knotx.java-library")
    id("io.knotx.unit-test")
    id("io.knotx.jacoco")
    id("io.knotx.maven-publish")
    id("io.knotx.release-java")
    id("org.nosphere.apache.rat")
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

// -----------------------------------------------------------------------------
// Dependencies
// -----------------------------------------------------------------------------
configurations {
    create("distConfig").extendsFrom(configurations.getByName("runtimeClasspath"))
}

dependencies {
    implementation(platform("io.knotx:knotx-dependencies:${project.version}"))
    implementation(group = "io.vertx", name = "vertx-core")
    implementation(group = "io.vertx", name = "vertx-rx-java2")
    implementation(group = "io.vertx", name = "vertx-config")
    implementation(group = "io.vertx", name = "vertx-config-hocon")
    implementation(group = "com.typesafe", name = "config")

    implementation(group = "ch.qos.logback", name = "logback-classic")

    implementation(group = "org.apache.commons", name = "commons-lang3")
    implementation(group = "com.google.guava", name = "guava")

    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api")
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-params")
    testImplementation(group = "io.vertx", name = "vertx-unit")
}
// -----------------------------------------------------------------------------
// Source sets
// -----------------------------------------------------------------------------
tasks {
    getByName<JavaCompile>("compileJava") {
        options.annotationProcessorGeneratedSourcesDirectory = file("src/main/generated")
    }
    getByName<Delete>("clean") {
        delete.add("src/main/generated")
    }
    getByName("rat").dependsOn("compileJava")
    getByName("sourcesJar").dependsOn("compileJava")
}
sourceSets.named("main") {
    java.srcDir("src/main/generated")
}
sourceSets.named("test") {
    resources.srcDir("conf")
}

// -----------------------------------------------------------------------------
// Tasks
// -----------------------------------------------------------------------------
tasks {
    //FIXME there is race condition with copying Version to generated and compiling project
//    register<Copy>("templatesProcessing") {
//        val now = Date().time
//        val tokens = mapOf("project.version" to project.version, "build.timestamp" to "${now}")
//        inputs.properties(tokens)
//
//        from("src/main/java-templates") {
//            include("*.java")
//            filter<ReplaceTokens>("tokens" to tokens)
//        }
//        into("src/main/generated/io/knotx/launcher")
//    }
//    getByName<JavaCompile>("compileJava").dependsOn("templatesProcessing")

    named<RatTask>("rat") {
        excludes.addAll(listOf(
            "**/*.md", // docs
            "gradle/wrapper/**", "gradle*", "**/build/**", // Gradle
            "*.iml", "*.ipr", "*.iws", "*.idea/**", // IDEs
            "**/generated/*", "**/*.adoc", "**/resources/**", // assets
            ".github/*", "conf/*.json", "conf/*.conf"
        ))
    }
    getByName("build").dependsOn("rat")
    getByName("rat").dependsOn("compileJava")

    register<Copy>("distScript") {
        from("script")
        into("$buildDir/dist")
    }
    register<Copy>("distConf") {
        from("conf")
        into("$buildDir/dist/conf")
    }
    register<Copy>("dist") {
        from(configurations.named("distConfig"), "$buildDir/libs")
        include("*.jar")
        exclude("*javadoc*", "*sources*", "*tests*")
        into("$buildDir/dist/lib")
    }
    getByName("distScript").dependsOn("build")
    getByName("distConf").dependsOn("distScript")
    getByName("dist").dependsOn("distConf")
}
// -----------------------------------------------------------------------------
// Publication
// -----------------------------------------------------------------------------
tasks.register<Zip>("distZip") {
    from("$buildDir/dist")
}
tasks.getByName("distZip").dependsOn("dist")

publishing {
    publications {
        withType(MavenPublication::class) {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
        }
    }
}
