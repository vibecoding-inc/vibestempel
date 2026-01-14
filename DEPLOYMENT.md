# Vibestempel Deployment Guide

This comprehensive guide covers the complete deployment process for the Vibestempel Android app with Supabase backend.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Supabase Setup](#supabase-setup)
3. [Application Configuration](#application-configuration)
4. [Building the Application](#building-the-application)
5. [Deployment Options](#deployment-options)
6. [Production Considerations](#production-considerations)
7. [Monitoring and Maintenance](#monitoring-and-maintenance)
8. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Accounts and Tools

- **Supabase Account**: Sign up at [https://supabase.com](https://supabase.com)
- **Android Studio**: Arctic Fox or later
- **JDK**: Version 17 or later
- **Android SDK**: API level 34
- **Git**: For version control

### Optional Tools

- **Google Play Console** account (for Play Store deployment)
- **Firebase** account (for analytics, crash reporting - optional)

---

## Supabase Setup

### Step 1: Create Supabase Project

1. **Sign in** to [Supabase](https://supabase.com)
2. Click **"New Project"**
3. Fill in project details:
   ```
   Name: vibestempel
   Database Password: [Choose a strong password - SAVE THIS!]
   Region: [Select closest to your target users]
   Pricing Plan: Free (or Pro for production)
   ```
4. Click **"Create new project"**
5. Wait 2-3 minutes for project initialization

### Step 2: Configure Database Schema

#### Import the Schema

1. Navigate to **SQL Editor** in your Supabase dashboard
2. Open the file `supabase/schema.sql` from this repository
3. Copy the entire contents
4. Paste into the SQL Editor
5. Click **"Run"** (or press Ctrl/Cmd + Enter)

#### Verify Tables Created

Go to **Database** > **Tables** and verify:

- ✅ `users` - Stores device-based user records
- ✅ `events` - Admin-created stamp collection events
- ✅ `stamps` - User stamp collection records

Go to **Database** > **Views** and verify:

- ✅ `user_stamp_counts` - Aggregated statistics view

### Step 3: Enable Realtime

**Critical for admin dashboard functionality!**

1. Go to **Database** > **Replication**
2. Enable replication for these tables:
   - ✅ `users`
   - ✅ `events`
   - ✅ `stamps`
3. Click **"Enable replication"** for each table

### Step 4: Configure Row Level Security (RLS)

The schema automatically enables RLS. Verify policies:

1. Go to **Authentication** > **Policies**
2. Check each table has policies:

**users table:**
- "Users can read own data"
- "Users can insert own data"
- "Users can update own data"

**events table:**
- "Everyone can read active events"
- "Admins can create events"
- "Admins can update events"

**stamps table:**
- "Users can read own stamps"
- "Users can insert own stamps"
- "Admins can read all stamps"

### Step 5: Get API Credentials

1. Go to **Settings** > **API**
2. Copy these values (you'll need them later):
   ```
   Project URL: https://your-project-id.supabase.co
   anon public key: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```

**⚠️ Security Note:** 
- Use the **anon public** key (not service_role!)
- The anon key is safe for client-side use
- Never commit credentials to version control

---

## Application Configuration

### Development Configuration

#### 1. Create local.properties

In the project root, create `local.properties`:

```properties
# Supabase Configuration
supabase.url=https://your-project-id.supabase.co
supabase.key=your-anon-key-here
```

**Important:** 
- This file is in `.gitignore` and won't be committed
- Copy from `local.properties.example` template
- Replace with your actual Supabase credentials

#### 2. Verify Build Configuration

The app uses BuildConfig to load credentials:

**File:** `app/build.gradle.kts`

```kotlin
buildConfigField("String", "SUPABASE_URL", "\"${properties.getProperty("supabase.url", "")}\"")
buildConfigField("String", "SUPABASE_KEY", "\"${properties.getProperty("supabase.key", "")}\"")
```

This keeps credentials out of source code.

### Production Configuration

#### Option 1: Environment Variables (CI/CD)

For GitHub Actions or other CI/CD:

```yaml
env:
  SUPABASE_URL: ${{ secrets.SUPABASE_URL }}
  SUPABASE_KEY: ${{ secrets.SUPABASE_KEY }}
```

Update `build.gradle.kts` release build type:

```kotlin
buildConfigField("String", "SUPABASE_URL", "\"${System.getenv("SUPABASE_URL") ?: ""}\"")
buildConfigField("String", "SUPABASE_KEY", "\"${System.getenv("SUPABASE_KEY") ?: ""}\"")
```

#### Option 2: Gradle Properties

Create `~/.gradle/gradle.properties`:

```properties
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_KEY=your-anon-key
```

---

## Building the Application

### Debug Build (Development)

```bash
# Clean previous builds
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# APK location:
# app/build/outputs/apk/debug/app-debug.apk
```

### Release Build (Production)

#### 1. Create Keystore (First Time Only)

```bash
keytool -genkey -v -keystore vibestempel-release.keystore \
  -alias vibestempel \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

**Save the keystore file and passwords securely!**

#### 2. Configure Signing

Create `keystore.properties` in project root:

```properties
storeFile=/path/to/vibestempel-release.keystore
storePassword=your-store-password
keyAlias=vibestempel
keyPassword=your-key-password
```

Add to `.gitignore`:
```
keystore.properties
*.keystore
```

#### 3. Update build.gradle.kts

Add signing config:

```kotlin
android {
    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties()
                keystoreProperties.load(FileInputStream(keystorePropertiesFile))
                
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ... other config
        }
    }
}
```

#### 4. Build Release APK

```bash
./gradlew assembleRelease

# APK location:
# app/build/outputs/apk/release/app-release.apk
```

---

## Deployment Options

### Option 1: Direct APK Distribution

**Best for:** Internal testing, small organizations

1. Build release APK (see above)
2. Distribute via:
   - Email
   - File sharing (Google Drive, Dropbox)
   - Internal server/website
3. Users install via:
   - Download APK
   - Enable "Install from unknown sources"
   - Install APK

**Pros:** Simple, no app store fees
**Cons:** Manual updates, security warnings

### Option 2: Google Play Store

**Best for:** Public release, automatic updates

#### Prerequisites

1. Google Play Developer account ($25 one-time fee)
2. Signed release APK or AAB
3. App assets (icon, screenshots, description)

#### Steps

1. **Create Play Console Account**
   - Visit [Google Play Console](https://play.google.com/console)
   - Pay one-time registration fee

2. **Create Application**
   - Click "Create app"
   - Fill in app details:
     ```
     App name: Vibestempel
     Default language: German
     App type: App
     Category: Education
     ```

3. **Prepare Store Listing**
   - **App icon:** 512x512 PNG
   - **Feature graphic:** 1024x500 PNG
   - **Screenshots:** At least 2 (phone)
   - **Description:** See `README.md` for content
   - **Privacy policy:** Required if collecting user data

4. **Set Up App Releases**
   
   **Internal Testing Track** (recommended first):
   ```bash
   # Build App Bundle (AAB - preferred by Play Store)
   ./gradlew bundleRelease
   
   # Location: app/build/outputs/bundle/release/app-release.aab
   ```

5. **Upload to Internal Testing**
   - Go to "Testing" > "Internal testing"
   - Create new release
   - Upload AAB file
   - Add release notes
   - Save and review
   - Rollout to internal testers

6. **Production Release**
   - After testing, promote to Production
   - Set countries/regions
   - Submit for review (1-3 days)

### Option 3: Firebase App Distribution

**Best for:** Beta testing, staged rollouts

1. **Set up Firebase project**
   ```bash
   # Install Firebase CLI
   npm install -g firebase-tools
   
   # Login
   firebase login
   
   # Initialize project
   firebase init
   ```

2. **Add Firebase to Android app**
   - Add google-services.json
   - Update build.gradle

3. **Distribute via Firebase**
   ```bash
   firebase appdistribution:distribute app-release.apk \
     --app your-firebase-app-id \
     --groups testers
   ```

### Option 4: GitHub Releases

**Best for:** Open source, version tracking

Already configured! The repo has GitHub Actions workflow:

**File:** `.github/workflows/android-build.yml`

Automatically builds APK on:
- Push to main
- Push to develop
- Push to copilot branches

Access builds:
1. Go to repository **Actions** tab
2. Click on latest workflow run
3. Download APK artifact

---

## Production Considerations

### Security Checklist

- [ ] Change default admin token from `admin123`
- [ ] Use strong, unique admin password
- [ ] Store keystore securely (backup!)
- [ ] Never commit credentials to git
- [ ] Use Supabase anon key (not service_role)
- [ ] Enable RLS policies in Supabase
- [ ] Consider implementing Supabase Auth for users
- [ ] Use ProGuard/R8 for code obfuscation

### Performance Optimization

**Enable ProGuard/R8:**

```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

**Add ProGuard rules** (`proguard-rules.pro`):

```proguard
# Supabase
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class com.vibestempel.app.**$$serializer { *; }
```

### Monitoring

**Recommended tools:**

1. **Supabase Dashboard**
   - Monitor database usage
   - Check API requests
   - Review realtime connections

2. **Google Play Console**
   - Crash reports
   - ANR (App Not Responding) reports
   - User reviews

3. **Firebase Crashlytics** (optional)
   ```kotlin
   dependencies {
       implementation("com.google.firebase:firebase-crashlytics-ktx")
   }
   ```

### Scaling Considerations

**Supabase Free Tier Limits:**
- 500 MB database
- 1 GB file storage
- 2 GB bandwidth/month
- Up to 50,000 monthly active users

**When to upgrade:**
- Database size > 400 MB
- More than 40,000 active users
- Need dedicated compute resources

**Supabase Pro:** $25/month
- 8 GB database
- 100 GB file storage
- 250 GB bandwidth
- Automatic backups
- Better performance

---

## Monitoring and Maintenance

### Daily Monitoring

1. **Check Supabase Dashboard**
   - Database size and growth
   - API request patterns
   - Error rates

2. **Monitor Play Console** (if published)
   - Crash rates (< 1% is good)
   - ANR rate
   - User ratings and reviews

### Weekly Tasks

1. **Review Supabase logs**
   - Look for unusual patterns
   - Check for failed queries
   - Monitor realtime connections

2. **Check user feedback**
   - Play Store reviews
   - Support emails
   - Social media mentions

### Monthly Tasks

1. **Database maintenance**
   - Review table sizes
   - Check index performance
   - Vacuum database (if needed)

2. **Update dependencies**
   ```bash
   ./gradlew dependencyUpdates
   ```

3. **Security updates**
   - Check for Supabase updates
   - Update Kotlin SDK versions

### Backup Strategy

**Supabase Backups:**
- Free tier: Daily backups (7-day retention)
- Pro tier: Point-in-time recovery

**Manual Backup:**
```bash
# Export database
pg_dump -h db.your-project.supabase.co \
  -U postgres \
  -d postgres \
  > backup-$(date +%Y%m%d).sql
```

---

## Troubleshooting

### Common Issues

#### Build Fails

**Error:** `SUPABASE_URL not found`

**Solution:**
- Verify `local.properties` exists
- Check credentials are correct
- Rebuild project: `./gradlew clean build`

#### Realtime Not Working

**Problem:** Admin dashboard doesn't update live

**Solutions:**
1. Check Realtime is enabled in Supabase
2. Verify replication enabled for tables
3. Check network connectivity
4. Review Supabase logs for errors

#### Stamp Collection Fails

**Error:** "Failed to add stamp"

**Solutions:**
1. Check internet connection
2. Verify Supabase URL and key
3. Check RLS policies
4. Review user_id in database

#### App Crashes on Launch

**Solutions:**
1. Check BuildConfig values
2. Verify Supabase credentials
3. Review crash logs in Play Console
4. Test with debug build

### Getting Help

1. **Supabase Discord**: [discord.supabase.com](https://discord.supabase.com)
2. **Supabase Docs**: [supabase.com/docs](https://supabase.com/docs)
3. **GitHub Issues**: Report bugs in this repository
4. **Stack Overflow**: Tag questions with `supabase` and `android`

---

## Appendix: Environment-Specific Configs

### Development Environment

```properties
# local.properties
supabase.url=https://dev-project.supabase.co
supabase.key=dev-anon-key
```

### Staging Environment

```properties
# Use build variants in build.gradle.kts
flavorDimensions += "environment"
productFlavors {
    create("staging") {
        dimension = "environment"
        buildConfigField("String", "SUPABASE_URL", "\"https://staging.supabase.co\"")
    }
}
```

### Production Environment

```bash
# CI/CD environment variables
export SUPABASE_URL="https://prod.supabase.co"
export SUPABASE_KEY="prod-anon-key"
```

---

## Quick Reference

### Essential Commands

```bash
# Clean build
./gradlew clean

# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run on device
./gradlew installDebug

# View dependencies
./gradlew dependencies
```

### Important Files

- `supabase/schema.sql` - Database schema
- `local.properties` - Local credentials (gitignored)
- `app/build.gradle.kts` - Build configuration
- `supabase/SETUP.md` - Detailed Supabase setup

### Support Resources

- 📚 [Supabase Docs](https://supabase.com/docs)
- 🎮 [Android Developer Guide](https://developer.android.com)
- 💬 [Supabase Discord](https://discord.supabase.com)
- 📖 [This Repository's README](../README.md)

---

**Last Updated:** January 2026  
**Version:** 2.0 (Direct Supabase Integration)
