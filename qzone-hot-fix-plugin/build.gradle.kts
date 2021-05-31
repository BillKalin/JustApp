plugins {
    id("kotlin")
    kotlin("kapt")
    `maven-publish`
}
dependencies {
    implementation("com.android.tools.build:gradle:3.6.1")
    kapt("com.google.auto.service:auto-service:1.0-rc6")
    implementation(gradleApi())
    implementation(project(path = ":just-app-asm"))
    implementation(project(path = ":just-app-common"))
    annotationProcessor("com.google.auto.service:auto-service:1.0-rc6")
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