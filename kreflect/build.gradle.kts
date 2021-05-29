plugins {
    id("kotlin")
}

dependencies {
    implementation(fileTree("dir" to "libs", "include" to "*.jar"))
    implementation(Depend.Kotlin.stdlib)
}
