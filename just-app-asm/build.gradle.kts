plugins {
    id("kotlin")
    kotlin("kapt")
    id("maven-publish")
}
dependencies {
    implementation("com.android.tools.build:gradle:3.6.1")
    kapt("com.google.auto.service:auto-service:1.0-rc6")
    api(project(path = ":just-app-common"))
    api("org.ow2.asm:asm:7.1")
    api("org.ow2.asm:asm-analysis:7.1")
    api("org.ow2.asm:asm-commons:7.1")
    api("org.ow2.asm:asm-tree:7.1")
    api("org.ow2.asm:asm-util:7.1")
    api("com.google.auto.service:auto-service:1.0-rc6")
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