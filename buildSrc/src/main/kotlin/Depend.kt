object Depend {
    object Kotlin {
        @JvmStatic
        val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlinVersion}"
    }

    object Support {
        @JvmStatic
        val appcompat = "androidx.appcompat:appcompat:${Versions.androidxVersion}"
        @JvmStatic
        val androidxCore = "androidx.core:core-ktx:1.2.0"
        @JvmStatic
        val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayoutVersion}"
    }
}