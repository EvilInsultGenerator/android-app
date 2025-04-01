plugins {
    id("com.android.application") version "8.9.1" apply false
    id("org.jetbrains.kotlin.android") version "2.1.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
}


tasks.register<Delete>("clean").configure {
    delete(rootProject.buildDir)
}