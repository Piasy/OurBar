plugins {
  kotlin("multiplatform")
  kotlin("native.cocoapods")
  kotlin("plugin.serialization")
  id("com.android.library")
}

version = "1.0"

kotlin {
  android()
  iosArm64()

  cocoapods {
    summary = "Some description for the Shared Module"
    homepage = "Link to the Shared Module homepage"
    ios.deploymentTarget = Vers.iosDeploymentTarget
    podfile = project.file("../iosApp/Podfile")
    framework {
      baseName = "OurBar"
    }
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Vers.kotlinxCoroutines}")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Vers.kotlinxSerialization}")

        implementation("io.ktor:ktor-client-core:${Vers.ktor}")
        implementation("io.ktor:ktor-client-logging:${Vers.ktor}")
      }
    }

    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))

        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Vers.kotlinxCoroutines}")
        implementation("io.mockk:mockk-common:${Vers.mockk}")
        implementation("io.ktor:ktor-client-mock:${Vers.ktor}")
      }
    }

    val androidMain by getting {
      dependencies {
        api("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Vers.kotlinxCoroutines}")

        implementation("com.tencent:mmkv:${Vers.mmkv}")

        api("io.ktor:ktor-client-okhttp:${Vers.ktor}")
        api("com.squareup.okhttp3:logging-interceptor:${Vers.okhttp}")

        api(project(":bt-android"))
      }
    }

    val androidTest by getting {
      dependencies {
        implementation("io.mockk:mockk:${Vers.mockk}")
      }
    }

    val iosArm64Main by getting
    val iosMain by creating {
      dependsOn(commonMain)
      iosArm64Main.dependsOn(this)

      dependencies {
        implementation("io.ktor:ktor-client-darwin:${Vers.ktor}")
      }
    }
    val iosArm64Test by getting
    val iosTest by creating {
      dependsOn(commonTest)
      iosArm64Test.dependsOn(this)
    }
  }
}

android {
  compileSdk = Vers.androidCompileSdk
  defaultConfig {
    minSdk = Vers.androidMinSdk

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].java.srcDir("src/androidMain/java")
    sourceSets["main"].jniLibs.srcDir("src/androidMain/jniLibs")
  }
}
