plugins {
    id("com.android.application") version "8.0.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
}


tasks.register<Delete>("clean").configure {
    delete(rootProject.buildDir)
}