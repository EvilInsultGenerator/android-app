plugins {
    id("com.android.application") version "8.3.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
}


tasks.register<Delete>("clean").configure {
    delete(rootProject.buildDir)
}