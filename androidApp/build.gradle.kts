plugins {
  id("com.android.application")
  kotlin("android")
  kotlin("kapt")
}

android {
  compileSdk = Vers.androidCompileSdk

  ndkVersion = Vers.androidNdk

  defaultConfig {
    minSdk = Vers.androidMinSdk
    targetSdk = Vers.androidTargetSdk

    versionCode = 1
    versionName = "1.0"

    applicationId = "com.piasy.ourbar.android"
    multiDexEnabled = true
  }
  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
    }
  }
}

dependencies {
  implementation("com.google.android.material:material:1.4.0")
  implementation("androidx.appcompat:appcompat:1.3.1")
  implementation("androidx.constraintlayout:constraintlayout:2.1.0")

  val permissionsDispatcher = "4.8.0"
  implementation("com.github.permissions-dispatcher:permissionsdispatcher:$permissionsDispatcher")
  kapt("com.github.permissions-dispatcher:permissionsdispatcher-processor:$permissionsDispatcher")

  implementation(project(":OurBar"))
}
