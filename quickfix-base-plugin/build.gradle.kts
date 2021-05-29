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
    annotationProcessor("com.google.auto.service:auto-service:1.0-rc6")
}

val group = "com.just.app.plugin"
val artifactName = "quickfix-base-plugin"
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
