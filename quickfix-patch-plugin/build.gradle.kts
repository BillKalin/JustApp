plugins {
    id("kotlin")
    `kotlin-dsl`
    `maven-publish`
}

gradlePlugin {
    plugins {
        register("quickfix-patch-plugin") {
            id = "quickfix-patch-plugin"
            implementationClass = "com.billkalin.plugin.QuickFixEntry"
        }
    }
}

dependencies {
    implementation(gradleApi())
    implementation("com.android.tools.build:gradle:3.6.1")
    implementation("org.javassist:javassist:3.28.0-GA")
}


group = "com.just.app.plugin"
version = "1.0.0"

publishing {
    repositories {
        maven {
            url = uri("../plugin")
        }
    }
}
