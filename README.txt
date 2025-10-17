ESP Scanner - Project sources

This is a minimal Android Studio project skeleton (Kotlin) that scans Wi-Fi networks and lists SSIDs starting with a customizable prefix (default: 'prise-').

How to build:
1. Open Android Studio (Arctic Fox or later recommended).
2. Choose 'Open an existing project' and select the folder ESPScannerApp.
3. Let Gradle sync. If Android Studio prompts to install Kotlin plugin or Gradle, accept.
4. Build > Build APK(s) or Run on a device.

Notes:
- The app requests ACCESS_FINE_LOCATION at runtime (required for Wi-Fi scans on modern Android).
- For Android 10+ some extra Wi-Fi management APIs differ; this app uses a simple approach: it opens system Wi-Fi settings when clicking an item.
- You can change the default filter prefix in the UI via the 'Filtre' button.
