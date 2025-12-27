plugins {
    id("com.android.application") version "8.13.0" apply false
    id("org.jetbrains.kotlin.android") version "2.1.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20" apply false
}


tasks.register<Delete>("clean").configure {
    delete(rootProject.buildDir)
}