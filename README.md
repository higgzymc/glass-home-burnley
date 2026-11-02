# Glass Home — Scenic

A lightweight Fire TV launcher with an alpine dusk wallpaper, a large clock tucked behind the mountain ridge, local weather, and your own app favourites. No advertising, recommendations, analytics SDKs or accounts.

## Download and install

[Download GlassHome-Scenic.apk](https://github.com/higgzymc/glass-home-burnley/raw/refs/heads/main/release/GlassHome-Scenic.apk)

On Windows, download this repository using **Code → Download ZIP**, extract it, and install Android SDK Platform Tools so `adb` is available. On the Fire Stick, enable **ADB debugging** under Developer Options and find its IP address under **About → Network**. Keep the computer and stick on the same local network.

From the extracted repository in PowerShell:

```powershell
.\Install-GlassHome.ps1 -StickIP 192.168.1.100
```

Replace the example address with the new stick's IP. If ADB is not on your PATH:

```powershell
.\Install-GlassHome.ps1 -StickIP 192.168.1.100 -Adb C:\platform-tools\adb.exe
```

Accept the debugging prompt on the TV. If the script stops while waiting for authorization, run it again afterwards. It installs the bundled APK, preserves any existing accessibility services, adds Glass Home's Home-button service, and opens the launcher. Previous accessibility settings are backed up locally. The installer does not disable any other apps.

For manual installation:

```powershell
adb connect FIRE_STICK_IP:5555
adb -s FIRE_STICK_IP:5555 install -r release/GlassHome-Scenic.apk
adb -s FIRE_STICK_IP:5555 shell am start -n local.glasshome/.MainActivity
```

The Home-button service needs enabling separately for manual installs. Use the installer above to preserve existing accessibility configuration. Amazon's launcher remains installed as a recovery route. Fire OS updates and different device models may affect this workaround. Tested on an AFTKM Fire Stick; Android 8 / API 26 or later is required. This is an Android APK and is not for non-Android Fire TV models.

## Remote controls and favourites

- **Select:** open an app.
- **Hold Select on an app:** choose **Add to Home** or **Remove from Home**. This works in All Apps and the Home dock. Removing a favourite does not uninstall it.
- **Menu:** switch between Home and All Apps.
- **Back:** return from All Apps to Home.
- **Left/right:** move through the dock; it scrolls when necessary.
- Select the weather in the top-right corner to refresh it, use automatic approximate location, or set a UK postcode/town.

Favourites are saved on each stick and survive app restarts and updates. Newly installed apps appear in All Apps; hold Select to pin them. Defaults, where installed: Disney+, Prime Video, YouTube, 5, BBC iPlayer, TiviMate, Channel 4, discovery+ and ITVX.

## Weather and privacy

Each stick automatically looks up its approximate location using its own internet connection. [ipwho.is](https://ipwhois.io/documentation) receives its public IP, and [Open-Meteo](https://open-meteo.com/en/docs) receives approximate latitude/longitude for current temperature and conditions. No GPS permission, API key or account is needed. A VPN can cause the VPN server's location to appear; use a postcode or manual town override if needed. UK postcode lookup uses Postcodes.io and sends the entered postcode to that service. Postcode mode stays at the saved location until changed; it does not track travel automatically.

Weather refreshes every five minutes while Home is active (or on manual refresh). Requests run off the UI thread with timeouts. Last successful conditions are cached and marked saved when a request fails or cached data is over ten minutes old. Home and app launching continue to work without internet. Weather data is provided by Open-Meteo under CC BY 4.0; its free API is intended for non-commercial use.

## Restore Amazon Home

Disable **Glass Home — Home button** in Accessibility settings. If editing via ADB, remove only `local.glasshome/local.glasshome.HomeService` from `enabled_accessibility_services`, preserving other services. The install script saves the original values in an ignored `accessibility-backup-*.json` file. Do not disable the entire accessibility system if other services use it.

## Build

JDK 17+ and Android SDK 35 are required. Run:

```powershell
.\gradlew.bat assembleDebug lintDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`. The bundled APK is a personal-use debug build; install subsequent bundled updates over it with `adb install -r`. A build signed with a different computer's debug key cannot update an existing installation without matching signing credentials.

## Artwork

The distributed APK uses the scenic alpine background. Burnley photos, the old Burnley APK, device addresses and saved weather locations are not included in this version. Older repository commits may still contain the previous Burnley edition.

The scenic background was made with the built-in image generation tool; see [SCENIC-UPDATE.md](SCENIC-UPDATE.md) for its prompt and asset path. The depth effect is tuned to this background. A per-device custom wallpaper can be stored as the app's private files/wallpaper.png; that disables mountain clipping and is never bundled into the shared APK.

Independent personal project; not affiliated with Apple or Amazon.

Version 1.4 opens native Fire TV settings categories directly and refreshes the clock at minute boundaries, on return to Home, and after time/timezone changes.

Version 1.5 supports per-stick wallpaper overrides. The downloadable APK always starts with the scenic background on a fresh installation.
