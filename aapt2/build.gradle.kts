plugins {
    id("kotlin")
    id("java-library")
}

dependencies {
    implementation(fileTree(mapOf("dir" to " libs", "include" to "*.jar")))
    implementation(Depend.Kotlin.stdlib)
    api("com.google.protobuf:protobuf-java:3.12.0-rc-1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}
