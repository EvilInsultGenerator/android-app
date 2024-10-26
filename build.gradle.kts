plugins {
    id("com.android.application") version "8.7.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.20" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
}


tasks.register<Delete>("clean").configure {
    delete(rootProject.buildDir)
}