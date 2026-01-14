# Vibestempel - Informatik Stempelpass App

An Android application for managing stamp collection for first-semester informatics students with beautiful animations and secure admin controls.

## ✨ Features

### Two User Modes

1. **Admin Mode** - Requires token authentication (default: `admin123`)
   - Create QR codes for events
   - Generate stamps for student check-ins
   - View current admin token
   - Beautiful material design interface

2. **User Mode** - No authentication required
   - Scan QR codes at events to receive stamps
   - View collected stamps with stunning animations
   - Track event attendance
   - Celebration animations when collecting stamps

### 🎨 Beautiful Design & Animations
- **Celebration Dialog**: Animated popup when collecting a new stamp
- **Smooth Transitions**: All screens feature elegant Material Design animations
- **Gradient Backgrounds**: Modern, eye-catching color schemes
- **Card-based UI**: Stamps displayed as beautiful cards with icons
- **Item Animations**: Stamps smoothly animate into view when displayed

## 🔒 Security Features

For detailed security information and admin mode usage, see [ADMIN_DOCUMENTATION.md](ADMIN_DOCUMENTATION.md).

- Token-based admin authentication
- Unique event IDs (UUID) prevent fake QR codes
- Duplicate stamp prevention
- QR code validation
- Private data storage

**⚠️ IMPORTANT**: Change the default admin token (`admin123`) before deploying!

## 🚀 CI/CD

The repository includes GitHub Actions workflows that automatically:

### Build Workflow
- Builds debug APK on every push
- Builds release APK for deployment
- Uploads APK artifacts for download
- Runs on push to main, develop, and copilot branches

### Release Workflow
- Triggers when a tag is created (e.g., `v1.0.0`)
- Builds release APK
- Creates a GitHub release with the tag name
- Uploads APK as a downloadable release asset
- Includes installation instructions in the release notes

To create a new release, simply push a tag:
```bash
git tag v1.0.0
git push origin v1.0.0
```

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
- JDK 17 or later
- Android SDK with API 34

### Build Steps
1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Run the app on an emulator or physical device

```bash
./gradlew assembleDebug
```

### CI/CD Build
The GitHub Actions workflow automatically builds APK files:
- **Debug APK**: For testing
- **Release APK**: For production deployment

APK artifacts are available in the Actions tab after each successful build.

## App Flow

1. **Launch**: Select Admin or User mode from beautiful main screen
2. **Admin Mode**: 
   - Enter admin token (default: `admin123`)
   - Create events and generate QR codes
   - Display QR codes with animated card interface
3. **User Mode**:
   - View collected stamps in animated list
   - Scan QR codes to collect new stamps
   - Enjoy celebration animation when stamp is received
   - Duplicate stamps are automatically prevented

## 📚 Documentation

- **[ADMIN_DOCUMENTATION.md](ADMIN_DOCUMENTATION.md)**: Comprehensive guide for administrators
  - How to use admin mode
  - Security best practices
  - Preventing user cheating
  - Changing admin token
  
- **[ARCHITECTURE.md](ARCHITECTURE.md)**: Technical architecture details
- **[IMPLEMENTATION.md](IMPLEMENTATION.md)**: Implementation notes

## Default Admin Token

⚠️ **Security Warning**: The default admin token is `admin123`. **You MUST change this** before using the app in production. See [ADMIN_DOCUMENTATION.md](ADMIN_DOCUMENTATION.md) for instructions.

## Permissions

- **Camera**: Required for scanning QR codes in user mode

## Screenshots

The app features:
- 🎨 Modern gradient color schemes (purple to pink)
- 🎉 Celebration animations when collecting stamps
- 📱 Material Design components throughout
- ✅ Beautiful stamp cards with icons and badges
- 🔒 Secure admin login interface

## Contributing

When contributing, please ensure:
- Code follows Kotlin style guidelines
- New features include appropriate animations
- Security features are not compromised
- Documentation is updated

## License

This project is created for educational purposes for first-semester informatics students.
