import org.jetbrains.gradle.ext.settings
import org.jetbrains.gradle.ext.taskTriggers

plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("kapt") version "2.1.10"
    id("com.gradleup.shadow") version "9.0.0-beta12"
    id("eclipse")
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.8"
    id("xyz.jpenilla.run-velocity") version "2.3.1"
}

group = "moe.mapetr"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc-repo"
    }
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    kapt("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.xerial:sqlite-jdbc:3.46.0.0")
    implementation("de.mkammerer:argon2-jvm:2.11")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        mergeServiceFiles()
        relocate("de.mkammerer", "moe.mapetr.uwuAuth.libs.argon2")
        exclude("module-info.class")
        exclude("META-INF/versions/*/module-info.class")
        manifest {
            attributes("Multi-Release" to "false")
        }
    }
    build {
        dependsOn(shadowJar)
    }
    runVelocity {
        velocityVersion("3.4.0-SNAPSHOT")
    }
}

kotlin {
    jvmToolchain(25)
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

val templateSource = file("src/main/templates")
val templateDest = layout.buildDirectory.dir("generated/sources/templates")
val generateTemplates = tasks.register<Copy>("generateTemplates") {
    val props = mapOf("version" to project.version)
    inputs.properties(props)

    from(templateSource)
    into(templateDest)
    expand(props)
}

sourceSets.main.configure { java.srcDir(generateTemplates.map { it.outputs }) }

project.idea.project.settings.taskTriggers.afterSync(generateTemplates)
project.eclipse.synchronizationTasks(generateTemplates)
