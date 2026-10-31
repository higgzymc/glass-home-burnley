plugins { id("com.android.application") }
android {
 namespace = "local.glasshome"
 compileSdk = 35
 defaultConfig { applicationId = "local.glasshome"; minSdk = 26; targetSdk = 30; versionCode = 1; versionName = "1.0" }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
