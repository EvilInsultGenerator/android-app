plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.20" apply false
}


tasks.register<Delete>("clean").configure {
    delete(rootProject.buildDir)
}