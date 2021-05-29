// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        jcenter()
        maven {
            url = (uri ("./plugin"))
        }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:3.6.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.70")
        classpath("io.johnsonlee.buildprops:buildprops-gradle-plugin:1.0.0")
        classpath("com.just.app.plugin:just-gradle-plugin:1.0.0")
        classpath("com.just.app.plugin:permission-check-plugin:1.0.0")
        classpath("com.just.app.plugin:r-inline-plugin:1.0.0")
        classpath("com.just.app.plugin:compress-res-plugin:1.0.0")
        classpath("com.just.app.plugin:list-artifact-plugin:1.0.0")
//        classpath( "com.just.app.plugin:qzone-hot-fix-plugin:1.0.0")
//        classpath ("com.just.app.plugin:quickfix-base-plugin:1.0.0")
        // NOTE: Do not place your application dependencies here; they  belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}

/*configurations.all {
    resolutionStrategy {
        this.eachDependency {
            val module = requested.module.toString()
            val moduleVersion = requested.version
            useVersion("")
        }
    }
}*/

tasks.register("clean", Delete::class.java) {
    delete(rootProject.buildDir)
}
