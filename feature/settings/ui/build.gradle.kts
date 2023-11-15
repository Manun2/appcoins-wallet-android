plugins {
  id("appcoins.android.library")
  id("appcoins.android.library.compose")
}

android { namespace = "com.appcoins.wallet.feature.settings.ui" }

dependencies {
  implementation(project(":feature:settings:data"))
  implementation(project(":feature:change-currency:data"))
  implementation(project(":feature:backup:ui"))
  implementation(project(":core:arch"))
  implementation(project(":core:shared-preferences"))
  implementation(project(":core:utils:android-common"))
  implementation(project(":core:utils:jvm-common"))
  implementation(project(":core:utils:properties"))
  implementation(project(":ui:common"))
  implementation(project(":ui:widgets"))
  implementation(project(":core:legacy-base"))
  implementation(project(":core:analytics"))
  implementation(project(":feature:support:data"))
  implementation(libs.kotlin.coroutines.rx2)
  implementation(libs.bundles.result)
  implementation(libs.bundles.androidx.compose)
  implementation(libs.bundles.rx)
  implementation(libs.androidx.fragment.ktx)
}