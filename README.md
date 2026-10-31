# Glass Home — Burnley edition

A Fire TV home screen with a large clock, a Burnley team photo, and five pinned apps: Disney+, TiviMate, YouTube, Luna, and Xbox. All Apps and Settings are in the dock.

## Build

Install Android Studio with Android SDK 35 and JDK 17. Open this folder in Android Studio, or run `gradlew.bat assembleDebug`. The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Install on a Fire TV Stick

Enable ADB debugging on the Stick and approve your computer on the TV. With Android platform tools installed, run:

```powershell
adb connect FIRE_STICK_IP:5555
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell settings put secure enabled_accessibility_services local.glasshome/local.glasshome.HomeService
adb shell settings put secure accessibility_enabled 1
adb shell am start -n local.glasshome/.MainActivity
```

The accessibility service returns to Glass Home when Fire TV opens Amazon's stock home screen. Fire OS updates may affect this behaviour. The stock launcher remains installed.

To restore Amazon Home, disable **Glass Home — Home button** under Accessibility settings, or use ADB to remove `local.glasshome/local.glasshome.HomeService` from `enabled_accessibility_services`.

The wallpaper photo was supplied by the device owner. Keep this repository private unless you have permission to share the photo.
