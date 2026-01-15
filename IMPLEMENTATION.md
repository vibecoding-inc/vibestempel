# Vibestempel App - Implementation Summary

## Overview
A complete Android application for managing a "Stempelpass" (stamp collection) system for first-semester informatics students.

## Implementation Details

### Architecture
- **Platform**: Native Android
- **Language**: Kotlin
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Build System**: Gradle with Kotlin DSL
- **Storage**: Supabase backend (PostgreSQL + Realtime)

### Key Features Implemented

#### 1. Mode Selection (MainActivity)
- Welcome screen with two buttons
- Admin Mode: Requires token authentication
- User Mode: Direct access to stamp collection

#### 2. Admin Mode
**Admin Login (AdminLoginActivity)**
- Token-based authentication
- Default token: `admin123` (stored in SharedPreferences)
- Token can be customized

**Admin Dashboard (AdminDashboardActivity)**
- Create events with name and description
- Generate QR codes for events
- Display generated QR codes for users to scan
- View current admin token
- **View realtime user stamp statistics**
- **See all users with their display names and stamp counts**
- Logout functionality

#### 3. User Mode
**User Dashboard (UserDashboardActivity)**
- View collected stamps in a list (RecyclerView)
- See total stamp count
- Button to scan new QR codes
- **Settings button to configure display name**
- Each stamp shows event name and timestamp
- Empty state message when no stamps exist

**QR Scanner (ScanQRActivity)**
- Camera-based QR code scanning
- Real-time QR code detection
- Automatic stamp collection upon successful scan
- Duplicate prevention (one stamp per event)

#### 4. User Management (NEW)
**User Profile Configuration**
- Users can set their display name via Settings button
- Names are stored in Supabase database
- Names persist across sessions and devices
- Dialog-based UI for simple name entry
- Validation to ensure names are not empty

**Admin View of Users**
- Admin dashboard displays all users with their configured names
- If no name is set, shows "User-[DeviceID]" as default
- Shows device ID as secondary information
- Realtime updates when users change their names
- Helps admins identify students by name instead of device ID

### Data Models

**Event**
- id: Unique identifier (UUID)
- name: Event name
- description: Event description
- timestamp: Creation time

**Stamp**
- eventId: Reference to event
- eventName: Name of the event
- timestamp: When stamp was collected

**SupabaseUser** (NEW)
- id: Unique user identifier (UUID)
- deviceId: Android device ID for authentication
- username: Optional display name configured by user
- createdAt: When user was first created
- updatedAt: When user profile was last updated

**UserStampCount** (NEW)
- userId: Reference to user
- deviceId: Device identifier
- username: User's configured display name
- stampCount: Total number of stamps collected
- lastStampCollected: Timestamp of most recent stamp

**SupabaseStorage**
- Manages all data persistence via Supabase
- Methods for adding stamps, checking duplicates
- **NEW: getUsername() - retrieves current user's display name**
- **NEW: updateUsername() - updates user's display name**
- getUserStampCounts() - gets all users with stamp statistics
- Realtime subscription for admin dashboard updates
- Admin token management (SharedPreferences)

**QRCodeGenerator**
- Generates QR codes from Event objects
- Uses ZXing library for encoding
- Parses scanned QR codes back to Event objects
- JSON serialization for QR code data

### UI Components

#### Layouts
1. `activity_main.xml` - Mode selection screen
2. `activity_admin_login.xml` - Admin authentication
3. `activity_admin_dashboard.xml` - QR code generation interface
4. `activity_user_dashboard.xml` - Stamp collection view
5. `item_stamp.xml` - Individual stamp card view

#### Themes & Resources
- Material Design Components
- German language strings
- Custom color scheme (purple/teal theme)
- App icon with adaptive icon support

### Dependencies
```kotlin
// Core Android
androidx.core:core-ktx:1.12.0
androidx.appcompat:appcompat:1.6.1
com.google.android.material:material:1.11.0
androidx.constraintlayout:constraintlayout:2.1.4

// QR Code Generation
com.google.zxing:core:3.5.2

// QR Code Scanning
com.journeyapps:zxing-android-embedded:4.3.0

// JSON Serialization
com.google.code.gson:gson:2.10.1
```

### Permissions
- **CAMERA**: Required for QR code scanning
- Runtime permission request implemented in UserDashboardActivity

### Security Features
1. Admin mode protected by token authentication
2. Token stored securely in SharedPreferences
3. Configurable admin token (default: `admin123`)
4. Duplicate stamp prevention

### User Flow

#### Admin Flow
1. Launch app → Select "Admin Modus"
2. Enter admin token → Login
3. Enter event details (name & description)
4. Generate QR code
5. Display QR code for users to scan

#### User Flow
1. Launch app → Select "Benutzer Modus"
2. View current stamps (if any)
3. Tap "QR-Code scannen"
4. Grant camera permission (first time)
5. Scan event QR code
6. Receive stamp confirmation
7. View new stamp in collection

### Data Storage Format
Stamps are stored in SharedPreferences as JSON:
```json
[
  {
    "eventId": "uuid-here",
    "eventName": "Welcome Event",
    "timestamp": 1705234567890
  }
]
```

QR Code data format:
```json
{
  "eventId": "uuid-here",
  "eventName": "Welcome Event",
  "description": "Erstsemester Begrüßung",
  "timestamp": 1705234567890
}
```

### Building the App

To build the app in Android Studio:
1. Open the project in Android Studio
2. Sync Gradle files
3. Run on emulator or physical device

Command line build (requires Android SDK):
```bash
./gradlew assembleDebug
```

### Testing Considerations
The app can be tested with:
- Android emulator (API 24+)
- Physical Android device (Android 7.0+)
- QR code generation and scanning can be tested by:
  1. Running admin mode on one device/emulator
  2. Running user mode on another device/emulator
  3. Scanning the generated QR code with a phone camera

### Files Created

**Kotlin Source Files (10)**
- MainActivity.kt
- AdminLoginActivity.kt
- AdminDashboardActivity.kt
- UserDashboardActivity.kt
- ScanQRActivity.kt
- StampsAdapter.kt
- Event.kt
- Stamp.kt
- StempelStorage.kt
- QRCodeGenerator.kt

**Layout Files (7)**
- activity_main.xml
- activity_admin_login.xml
- activity_admin_dashboard.xml
- activity_user_dashboard.xml
- item_stamp.xml
- item_user_stamp_count.xml
- dialog_set_username.xml (NEW)

**Resource Files (4)**
- strings.xml (German)
- themes.xml
- colors.xml
- ic_launcher_background.xml

**Configuration Files (7)**
- AndroidManifest.xml
- build.gradle.kts (project & app level)
- settings.gradle.kts
- gradle.properties
- proguard-rules.pro
- .gitignore

**Build Files (3)**
- gradlew
- gradle-wrapper.properties
- gradle-wrapper.jar

## Summary
The Vibestempel app is now fully implemented with all requested features:
- ✅ Two modes (Admin & User)
- ✅ Admin token authentication
- ✅ QR code generation (Admin)
- ✅ QR code scanning (User)
- ✅ Stamp collection and display
- ✅ Supabase backend with realtime updates
- ✅ Duplicate prevention (serverside)
- ✅ German UI
- ✅ **User profile management (display names)**
- ✅ **Admin can view user names and statistics**

The app is ready to build and deploy on Android devices running Android 7.0 or later.
