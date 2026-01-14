# Vibestempel - Admin Mode Documentation

## Table of Contents
1. [Admin Mode Overview](#admin-mode-overview)
2. [How to Access Admin Mode](#how-to-access-admin-mode)
3. [Creating Events and QR Codes](#creating-events-and-qr-codes)
4. [Security Features](#security-features)
5. [Preventing User Cheating](#preventing-user-cheating)
6. [Changing the Admin Token](#changing-the-admin-token)
7. [Best Practices](#best-practices)

---

## Admin Mode Overview

The Admin Mode is a protected area of the Vibestempel app that allows authorized users (event organizers, professors, or administrators) to:
- Create events
- Generate QR codes for events
- Display QR codes for students to scan
- View and change the admin authentication token

**Only users with the correct admin token can access this mode.**

---

## How to Access Admin Mode

### Step 1: Launch the App
Open the Vibestempel app on your Android device.

### Step 2: Select Admin Mode
From the main screen, tap the **"Admin Modus"** button.

### Step 3: Enter the Admin Token
You will be prompted to enter the admin token:
- **Default Token**: `admin123`
- Enter the token in the text field
- Tap the **"Login"** button

### Step 4: Access Granted
If the token is correct, you will be directed to the Admin Dashboard where you can create events and generate QR codes.

**⚠️ Important**: Keep your admin token secure and do not share it with students or unauthorized users.

---

## Creating Events and QR Codes

Once logged into the Admin Dashboard, you can create events:

### Step-by-Step Process

1. **Enter Event Name**
   - Type the name of your event (e.g., "Welcome Lecture", "Lab Session 1")
   - This will be displayed to users when they scan the QR code

2. **Enter Event Description**
   - Provide a brief description of the event
   - This helps identify the event and provides context

3. **Generate QR Code**
   - Tap the **"QR-Code generieren"** button
   - The app will create a unique QR code for this specific event

4. **Display the QR Code**
   - The generated QR code will appear on screen
   - Students can scan this QR code to collect their stamp
   - Each event has a unique ID to prevent duplicate stamps

### Tips for Event Creation
- Use clear, descriptive event names
- Create QR codes before the event starts
- Display the QR code prominently so students can scan easily
- You can create multiple QR codes for different events

---

## Security Features

The Vibestempel app implements multiple security measures to protect admin access and prevent cheating:

### 1. Token-Based Authentication
- **What it is**: Admin mode requires a secret token (password) to access
- **Default token**: `admin123` (should be changed immediately)
- **How it works**: The token is stored securely in the app's private SharedPreferences, which is only accessible to the app itself
- **Protection**: Users cannot access admin functions without knowing the token

### 2. Unique Event IDs
- **What it is**: Each QR code contains a unique event identifier (UUID)
- **How it works**: When an event is created, the app generates a universally unique identifier
- **Protection**: Even if users try to create fake QR codes, they cannot predict the UUID

### 3. Duplicate Prevention
- **What it is**: Users cannot collect the same stamp twice
- **How it works**: When a user scans a QR code, the app checks if they already have a stamp for that event ID
- **Protection**: If a duplicate is detected, the app rejects the stamp and shows a message

### 4. QR Code Validation
- **What it is**: The app validates QR code format and content
- **How it works**: QR codes must contain valid JSON with specific fields (eventId, eventName, description, timestamp)
- **Protection**: Random QR codes or improperly formatted codes are rejected

### 5. Private Storage
- **What it is**: All data is stored in app-private SharedPreferences
- **How it works**: Android's security model prevents other apps from accessing this data
- **Protection**: Users cannot modify stamp data directly through file managers or other apps

---

## Preventing User Cheating

### Common Cheating Attempts and How They're Prevented

#### 1. Screenshot Sharing
**Attempt**: A user takes a screenshot of a QR code and shares it with others.
- **Why it doesn't work**: While students can share screenshots, each user can still only scan each unique QR code once due to duplicate prevention. The admin should display QR codes only during the actual event.
- **Mitigation**: Display QR codes only during the event, and for a limited time.

#### 2. Creating Fake QR Codes
**Attempt**: A user tries to create their own QR code to collect stamps.
- **Why it doesn't work**: QR codes contain a unique UUID that only the admin app can generate. Without knowing the exact UUID format and valid event IDs, users cannot create working QR codes.
- **Protection Level**: ✅ High - UUIDs are cryptographically random and unpredictable.

#### 3. Scanning the Same QR Code Multiple Times
**Attempt**: A user tries to scan the same QR code multiple times to collect duplicate stamps.
- **Why it doesn't work**: The app checks each stamp's event ID before adding it. If a duplicate is found, the app rejects the stamp.
- **Protection Level**: ✅ Complete - Built-in duplicate prevention.

#### 4. Modifying App Data
**Attempt**: A user with a rooted device tries to modify the SharedPreferences file directly.
- **Why it's difficult**: The data is stored in a private app directory that requires root access to modify. Most users don't have rooted devices.
- **Limitation**: ⚠️ Users with rooted devices and technical knowledge could potentially modify data.
- **Note**: For highest security, consider implementing backend verification in a future version.

#### 5. Installing Modified/Hacked Versions
**Attempt**: A user installs a modified version of the app that bypasses checks.
- **Why it's difficult**: The app must be signed with the correct certificate to install. Modified apps won't receive updates.
- **Limitation**: ⚠️ Determined users could decompile and modify the app.
- **Mitigation**: Distribute the app through official channels only (Google Play Store).

#### 6. Accessing Admin Mode
**Attempt**: A user tries to guess or brute-force the admin token.
- **Why it's difficult**: The default token should be changed to a strong, unpredictable password.
- **Protection**: Strong token authentication prevents unauthorized admin access.
- **Required Action**: ⚠️ **CHANGE THE DEFAULT TOKEN IMMEDIATELY!**

---

## Changing the Admin Token

**⚠️ CRITICAL: You MUST change the default admin token before using the app in production!**

### Why Change the Token?
- The default token (`admin123`) is documented and publicly known
- Anyone reading this documentation could access admin mode with the default token
- Changing it ensures only authorized users can create QR codes

### How to Change the Token

1. **Access Admin Mode**
   - Login with the current token (default: `admin123`)

2. **View Current Token**
   - On the Admin Dashboard, you'll see "Aktueller Admin Token: [current token]"

3. **Change the Token**
   - Tap the **"Token ändern"** button
   - A dialog will appear asking for the new token
   - Enter a strong, secure token (recommended: at least 12 characters, mix of letters, numbers, and symbols)
   - Examples of strong tokens:
     - `Inf0rm@tik2024!Sicher`
     - `EventAdmin$2024#Secure`
     - `Vibe$tempel!Str0ng`

4. **Confirm the Change**
   - The token is immediately updated
   - Write down the new token in a secure location
   - **⚠️ If you forget the token, you cannot access admin mode!**

5. **Log Out and Test**
   - Tap "Abmelden" to log out
   - Try logging in again with the new token to confirm it works

### Token Recovery
**⚠️ IMPORTANT**: There is NO password recovery mechanism in this app!

If you forget your admin token:
- You will need to reinstall the app (which resets the token to `admin123`)
- All existing user stamps will be lost with a reinstall
- **Always keep your token in a secure, backed-up location!**

---

## Best Practices

### For Admins

1. **Change Default Token Immediately**
   - Never use `admin123` in production
   - Use a strong, unique password

2. **Keep Token Secure**
   - Don't share the token with students
   - Don't write it on whiteboards or public locations
   - Store it in a password manager

3. **Control QR Code Display**
   - Only show QR codes during actual events
   - Don't leave QR codes displayed when events end
   - Consider taking a screenshot and displaying it on a projector (and then deleting it)

4. **Create Unique Events**
   - Create a new event for each session/lecture
   - Use descriptive names so students know what they're collecting
   - Example: "Week 1 - Introduction to Programming"

5. **Backup Your Token**
   - Write down your admin token
   - Store it in a secure location (password manager, locked drawer)
   - Consider having a backup admin with a separate token

### For Deployment

1. **Distribution**
   - Distribute the app through official channels (Google Play, internal app store)
   - Avoid sharing APK files directly (users might get outdated versions)

2. **Communication**
   - Clearly communicate to students that each stamp can only be collected once
   - Explain that attempting to cheat will not work
   - Set expectations for stamp collection rules

3. **Monitoring**
   - Periodically check that QR codes are being scanned as expected
   - Be present when displaying QR codes to ensure only attending students scan them

4. **Future Enhancements**
   - Consider adding backend server verification for highest security
   - Implement time-limited QR codes that expire after the event
   - Add admin panels to view stamp statistics

---

## Security Summary

### What's Protected ✅
- ✅ Admin mode access (token authentication)
- ✅ Duplicate stamps (event ID checking)
- ✅ Fake QR codes (UUID validation)
- ✅ Multiple scans of same code (duplicate prevention)
- ✅ Data privacy (Android app sandboxing)

### Potential Vulnerabilities ⚠️
- ⚠️ Rooted devices (users with root could modify app data)
- ⚠️ App decompilation (technically skilled users could modify the app)
- ⚠️ QR code sharing (users can share screenshots, but limited by duplicate prevention)
- ⚠️ Default token (must be changed by admin)

### Recommended Additional Security (Future)
- Backend server with stamp verification
- Time-limited QR codes
- Admin dashboard to monitor stamp collection
- Certificate pinning to prevent man-in-the-middle attacks
- Encryption of stored stamp data
- Biometric authentication for admin mode

---

## Troubleshooting

### "Ungültiger Token!" Error
- **Cause**: The token you entered is incorrect
- **Solution**: Double-check your token for typos, ensure caps lock is off

### Cannot Access Admin Mode
- **Cause**: You forgot your token
- **Solution**: Reinstall the app (⚠️ this will reset all data!)

### QR Code Not Scanning
- **Cause**: Camera permissions not granted, or QR code format invalid
- **Solution**: Ensure camera permission is granted; regenerate the QR code

### Students Report "Already Have This Stamp"
- **Cause**: Student has already scanned this event's QR code
- **Solution**: This is expected behavior; each stamp can only be collected once

---

## Contact and Support

For technical issues or questions about admin mode:
- Review this documentation
- Check the main README.md for general app information
- Contact your system administrator or IT support

---

**Last Updated**: January 2024  
**Version**: 1.0  
**App**: Vibestempel - Informatik Stempelpass
