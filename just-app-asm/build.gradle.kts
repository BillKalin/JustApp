plugins {
    id("kotlin")
    kotlin("kapt")
    id("maven-publish")
}
dependencies {
    implementation("com.android.tools.build:gradle:3.6.1")
    kapt("com.google.auto.service:auto-service:1.0-rc6")
    api(project(path =  ":just-app-common"))
    api("org.ow2.asm:asm:7.1")
    api("org.ow2.asm:asm-analysis:7.1")
    api("org.ow2.asm:asm-commons:7.1")
    api( "org.ow2.asm:asm-tree:7.1")
    api("org.ow2.asm:asm-util:7.1")
    api("com.google.auto.service:auto-service:1.0-rc6")
}

val group = "com.just.app.plugin"
val artifactName = "just-app-asm"
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