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

group = "com.just.app.plugin"
version = "1.0.1"

publishing {
    repositories {
        maven {
            url = uri("../plugin")
        }
    }
}