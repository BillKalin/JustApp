plugins {
    id("kotlin")
    kotlin("kapt")
    `maven-publish`
    id("io.johnsonlee.buildprops")
    `kotlin-dsl`
}
gradlePlugin {
    plugins {
        register("just-gradle-plugin") {
            id = "just-gradle-plugin"
            implementationClass = "com.just.gradle.plugin.PluginEntry"
        }
    }
}

dependencies {
    implementation(gradleApi())
    implementation("com.android.tools.build:gradle:3.6.1")
    api(project(path = ":just-app-common"))
}

group = "com.just.app.plugin"
version = "1.0.1"


publishing {
    repositories {
        maven {
            url = uri("../plugin/")
        }
    }
}
