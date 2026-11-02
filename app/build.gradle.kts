plugins { id("com.android.application") }
android {
 namespace = "local.glasshome"
 compileSdk = 35
 // Sideloaded Fire TV app; preserve the existing Fire OS compatibility target.
 lint { disable += "ExpiredTargetSdkVersion" }
 defaultConfig { applicationId = "local.glasshome"; minSdk = 26; targetSdk = 30; versionCode = 6; versionName = "1.5" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
