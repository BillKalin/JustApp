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

val group = "com.just.app.plugin"
val artifactName = "just-gradle-plugin"
val versionName = "1.0.0"

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
