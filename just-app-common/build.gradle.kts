import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

plugins {
    java
    id("kotlin")
    kotlin("kapt")
    id("maven-publish")}

val compileKotlin: KotlinCompile<KotlinJvmOptions> by tasks
compileKotlin.kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()

dependencies {
    implementation(gradleApi())
    implementation("com.android.tools.build:gradle:3.6.1")
    compileOnly("com.android.tools.build:builder:3.6.1")
}

val group = "com.just.app.plugin"
val artifactName = "just-app-common"
val versionName = "1.0.1"

val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets.main.get().allSource)
}

publishing {
    repositories {
        maven {
            url = uri("../plugin")
        }
    }
    publications {
        register("mavenJava", MavenPublication::class.java) {
            from(components["java"])
//            artifact(sourcesJar.get())
            groupId = group
            this.version = versionName
            artifactId = artifactName
        }
    }
}