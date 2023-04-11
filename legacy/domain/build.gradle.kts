plugins {
  id("appcoins.android.library")
  id("kotlin-parcelize")
}

android {
  namespace = "com.appcoins.wallet.legacy.domain"
}

dependencies {
  compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))

  implementation(libs.androidx.recyclerview)
  implementation(libs.androidx.navigation.ui)
  implementation(libs.androidx.fragment.ktx)
  implementation(libs.androidx.fragment)

}
