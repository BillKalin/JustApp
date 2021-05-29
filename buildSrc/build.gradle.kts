repositories {
    jcenter()
    google()
}

plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("quick-fix") {
            id = "quick-fix"
            implementationClass = "com.billkalin.plugin.QuickFixEntry"
        }
    }
}

dependencies {
    gradleApi()
    implementation("com.android.tools.build:gradle:3.6.1")
//    implementation("org.ow2.asm:asm:7.1")
    implementation("org.javassist:javassist:3.28.0-GA")
}
