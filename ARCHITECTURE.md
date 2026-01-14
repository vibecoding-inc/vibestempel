# Vibestempel App - Architecture & Flow Diagram

## App Flow

```
┌─────────────────────────────────────────────────────────────────────┐
│                          VIBESTEMPEL APP                            │
└─────────────────────────────────────────────────────────────────────┘

                              Launch
                                 │
                                 ▼
                    ┌────────────────────────┐
                    │    MainActivity        │
                    │  (Mode Selection)      │
                    └────────────────────────┘
                                 │
                    ┌────────────┴────────────┐
                    │                         │
              Admin Mode                 User Mode
                    │                         │
                    ▼                         ▼
       ┌─────────────────────┐   ┌─────────────────────────┐
       │ AdminLoginActivity  │   │ UserDashboardActivity   │
       │  - Token Input      │   │  - View Stamps          │
       │  - Validate Token   │   │  - Scan Button          │
       └─────────────────────┘   └─────────────────────────┘
                    │                         │
              (auth success)            (scan button)
                    │                         │
                    ▼                         ▼
     ┌──────────────────────────┐  ┌─────────────────────┐
     │ AdminDashboardActivity   │  │  ScanQRActivity     │
     │  - Event Name Input      │  │  - Camera View      │
     │  - Event Desc Input      │  │  - QR Scanner       │
     │  - Generate QR Button    │  └─────────────────────┘
     │  - Display QR Code       │             │
     │  - Show Admin Token      │       (scan success)
     └──────────────────────────┘             │
                    │                         │
           (generate button)                  ▼
                    │               ┌─────────────────┐
                    ▼               │  Add Stamp to   │
        ┌──────────────────┐        │  Local Storage  │
        │  QR Code Image   │        └─────────────────┘
        │  (Event Data)    │                 │
        └──────────────────┘                 │
                                             ▼
                                  ┌──────────────────────┐
                                  │ Show Stamp in List   │
                                  │  (UserDashboard)     │
                                  └──────────────────────┘
```

## Component Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        UI LAYER                                 │
├─────────────────────────────────────────────────────────────────┤
│  MainActivity                                                   │
│  AdminLoginActivity                                             │
│  AdminDashboardActivity                                         │
│  UserDashboardActivity                                          │
│  ScanQRActivity                                                 │
│  StampsAdapter (RecyclerView)                                   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    BUSINESS LOGIC LAYER                         │
├─────────────────────────────────────────────────────────────────┤
│  QRCodeGenerator                                                │
│    - generateQRCode(Event) → Bitmap                             │
│    - parseQRCode(String) → Event                                │
│                                                                 │
│  StempelStorage                                                 │
│    - addStamp(Stamp) → Boolean                                  │
│    - getStamps() → List<Stamp>                                  │
│    - hasStampForEvent(String) → Boolean                         │
│    - validateAdminToken(String) → Boolean                       │
│    - setAdminToken(String)                                      │
│    - getAdminToken() → String                                   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                       DATA LAYER                                │
├─────────────────────────────────────────────────────────────────┤
│  Event (id, name, description, timestamp)                       │
│  Stamp (eventId, eventName, timestamp)                          │
│                                                                 │
│  SharedPreferences                                              │
│    - stamps: JSON array                                         │
│    - admin_token: String                                        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    EXTERNAL LIBRARIES                           │
├─────────────────────────────────────────────────────────────────┤
│  ZXing Core (QR Generation)                                     │
│  ZXing Android Embedded (QR Scanning)                           │
│  Gson (JSON Serialization)                                      │
│  Material Components (UI)                                       │
└─────────────────────────────────────────────────────────────────┘
```

## Data Flow

### Admin Creating QR Code
```
1. Admin enters event details (name, description)
2. App creates Event object with UUID
3. QRCodeGenerator converts Event to JSON
4. ZXing encodes JSON into QR code bitmap
5. QR code displayed on screen for users to scan
```

### User Scanning QR Code
```
1. User taps "Scan QR Code" button
2. App requests camera permission (if needed)
3. ScanQRActivity opens with camera preview
4. ZXing scans and decodes QR code to JSON string
5. QRCodeGenerator parses JSON to Event object
6. App creates Stamp from Event
7. StempelStorage checks for duplicate
8. If unique, Stamp saved to SharedPreferences
9. User sees confirmation toast
10. Returns to UserDashboard with updated list
```

## Security Model

```
┌───────────────────────────────────────────┐
│         Admin Token Protection            │
├───────────────────────────────────────────┤
│  Default Token: "admin123"                │
│  Storage: SharedPreferences (private)     │
│  Validation: String comparison            │
│  Can be changed by admin                  │
└───────────────────────────────────────────┘

┌───────────────────────────────────────────┐
│        Duplicate Prevention               │
├───────────────────────────────────────────┤
│  Check: eventId in existing stamps        │
│  Action: Reject if duplicate              │
│  Message: "Already have this stamp"       │
└───────────────────────────────────────────┘
```

## File Structure

```
vibestempel/
├── app/
│   ├── src/main/
│   │   ├── java/com/vibestempel/app/
│   │   │   ├── Activities/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── AdminLoginActivity.kt
│   │   │   │   ├── AdminDashboardActivity.kt
│   │   │   │   ├── UserDashboardActivity.kt
│   │   │   │   └── ScanQRActivity.kt
│   │   │   ├── Models/
│   │   │   │   ├── Event.kt
│   │   │   │   └── Stamp.kt
│   │   │   ├── Utils/
│   │   │   │   ├── QRCodeGenerator.kt
│   │   │   │   └── StempelStorage.kt
│   │   │   └── Adapters/
│   │   │       └── StampsAdapter.kt
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml
│   │   │   │   ├── activity_admin_login.xml
│   │   │   │   ├── activity_admin_dashboard.xml
│   │   │   │   ├── activity_user_dashboard.xml
│   │   │   │   └── item_stamp.xml
│   │   │   ├── values/
│   │   │   │   ├── strings.xml
│   │   │   │   ├── colors.xml
│   │   │   │   └── themes.xml
│   │   │   └── drawable/
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── README.md
├── IMPLEMENTATION.md
└── .gitignore
```
