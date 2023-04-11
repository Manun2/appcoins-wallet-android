plugins {
  id("appcoins.android.library")
  id("kotlin-parcelize")
}

android {
  namespace = "com.appcoins.wallet.feature.home"
  defaultConfig {
    buildFeatures {
      viewBinding = true
      composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.androidx.compose.asProvider().get()
      }
      compose = true
    }
  }
}

dependencies {
  compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
  implementation(project(":ui:common"))
  implementation(project(":ui:arch"))
  implementation(project(":ui:widgets"))
  implementation(project(":core:analytics"))
  implementation(project(":core:utils:android-common"))
  implementation(project(":feature:intercom"))
  implementation(project(":legacy:domain"))


  implementation(libs.androidx.recyclerview)
  implementation(libs.androidx.navigation.ui)
  implementation(libs.androidx.fragment.ktx)
  implementation(libs.androidx.fragment)
  implementation(libs.bundles.androidx.compose)

  implementation(libs.zxing.android)
  implementation(libs.glide)
  implementation(libs.epoxy)
  kapt(libs.epoxy.processor)
  implementation(libs.viewbinding.delegate)
  implementation(libs.androidx.appcompact)
  implementation(libs.intercom) {
    exclude(group = "com.google.android", module = "flexbox")
  }
}
