# Vibestempel - Informatik Stempelpass App

An Android application for managing stamp collection for first-semester informatics students.

## Features

### Two User Modes

1. **Admin Mode** - Requires token authentication (default: `admin123`)
   - Create QR codes for events
   - Generate stamps for student check-ins
   - View current admin token

2. **User Mode** - No authentication required
   - Scan QR codes at events to receive stamps
   - View collected stamps
   - Track event attendance

## Technical Details

- **Platform**: Android (minSdk 24, targetSdk 34)
- **Language**: Kotlin
- **Architecture**: Native Android with local storage (SharedPreferences)
- **Key Libraries**:
  - ZXing for QR code generation and scanning
  - Material Design Components
  - AndroidX libraries

## Building the App

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 8 or later
- Android SDK with API 34

### Build Steps
1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Run the app on an emulator or physical device

```bash
./gradlew build
```

## App Flow

1. **Launch**: Select Admin or User mode
2. **Admin Mode**: 
   - Enter admin token (default: `admin123`)
   - Create events and generate QR codes
   - Display QR codes for users to scan
3. **User Mode**:
   - View collected stamps
   - Scan QR codes to collect new stamps
   - Duplicate stamps are automatically prevented

## Default Admin Token

The default admin token is `admin123`. This can be changed within the admin dashboard.

## Permissions

- **Camera**: Required for scanning QR codes in user mode