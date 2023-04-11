plugins {
  id("appcoins.android.library")
  id("kotlin-parcelize")
}

android {
  namespace = "com.appcoins.wallet.feature.home"
}

dependencies {

  implementation(libs.intercom) {
    exclude(group = "com.google.android", module = "flexbox")
  }
}
