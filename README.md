# Vibestempel - Informatik Stempelpass App

An Android application for managing stamp collection for first-semester informatics students with beautiful animations and secure admin controls.

## ✨ Features

### Two User Modes

1. **Admin Mode** - Requires token authentication (default: `admin123`)
   - Create QR codes for events
   - Generate stamps for student check-ins
   - View current admin token
   - **NEW**: View realtime user stamp statistics
   - **NEW**: See live count of stamps per user
   - Beautiful material design interface

2. **User Mode** - No authentication required
   - Scan QR codes at events to receive stamps
   - View collected stamps with stunning animations
   - Track event attendance
   - Celebration animations when collecting stamps
   - **NEW**: All stamps synced to cloud via Supabase

### 🎨 Beautiful Design & Animations
- **Celebration Dialog**: Animated popup when collecting a new stamp
- **Smooth Transitions**: All screens feature elegant Material Design animations
- **Gradient Backgrounds**: Modern, eye-catching color schemes
- **Card-based UI**: Stamps displayed as beautiful cards with icons
- **Item Animations**: Stamps smoothly animate into view when displayed

## 🔒 Security Features

For detailed security information and admin mode usage, see [ADMIN_DOCUMENTATION.md](ADMIN_DOCUMENTATION.md).

- Token-based admin authentication
- **Serverside validation via Supabase**
- **Supabase Row Level Security (RLS) policies**
- Unique event IDs (UUID) prevent fake QR codes
- Duplicate stamp prevention (serverside)
- QR code validation
- **Device-based user identification**
- **Credentials stored in BuildConfig (not in code)**

**⚠️ IMPORTANT**: Change the default admin token (`admin123`) before deploying!

## 🚀 CI/CD

The repository includes GitHub Actions workflow that automatically:
- Builds debug APK on every push
- Builds release APK for deployment
- Uploads APK artifacts for download
- Runs on push to main, develop, and copilot branches

## Technical Details

- **Platform**: Android (minSdk 24, targetSdk 34)
- **Language**: Kotlin
- **Architecture**: Native Android with **Supabase backend**
- **Backend**: Supabase (PostgreSQL + Realtime)
- **Key Libraries**:
  - ZXing for QR code generation and scanning
  - Material Design Components
  - AndroidX libraries
  - Kotlin Coroutines for async operations
  - Supabase Kotlin SDK for database and realtime

## Building the App

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 17 or later
- Android SDK with API 34
- **Supabase project** (see setup below)

### Supabase Backend Setup

1. **Create Supabase Project**: Follow [`supabase/SETUP.md`](supabase/SETUP.md)
2. **Configure App**: Copy `local.properties.example` to `local.properties` and add your Supabase credentials

### Build Steps
1. Clone the repository
2. Set up Supabase backend (see above)
3. Open the project in Android Studio
4. Sync Gradle files
5. Run the app on an emulator or physical device

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

- **[DEPLOYMENT.md](DEPLOYMENT.md)**: **NEW!** Complete deployment guide
  - Supabase setup and configuration
  - Building and signing the app
  - Deployment options (Play Store, APK, Firebase)
  - Production considerations
  - Monitoring and maintenance
- **[supabase/SETUP.md](supabase/SETUP.md)**: Detailed Supabase database configuration
- **[ADMIN_DOCUMENTATION.md](ADMIN_DOCUMENTATION.md)**: Comprehensive guide for administrators
  - How to use admin mode
  - Security best practices
  - Preventing user cheating
  - Changing admin token
  - Viewing realtime user stamp statistics
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
