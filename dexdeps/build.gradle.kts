plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes("Main-Class" to "com.billkalin.dex.Main", "version" to "1.0.0")
    }
}