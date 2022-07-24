buildscript {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }
  dependencies {
    classpath("com.android.tools.build:gradle:${Vers.agp}")

    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Vers.kotlin}")
    classpath("org.jetbrains.kotlin:kotlin-serialization:${Vers.kotlin}")
  }
}

allprojects {
  repositories {
    mavenCentral()
    google()

    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
    maven { url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev") }
  }

  configurations {
    all {
      exclude(group = "org.json", module = "json")
    }
  }
}

tasks.register("clean", Delete::class) {
  delete(rootProject.buildDir)
}
