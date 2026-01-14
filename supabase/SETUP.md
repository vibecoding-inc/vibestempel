# Supabase Setup Guide for Vibestempel

This guide provides detailed steps to set up Supabase as the backend for the Vibestempel Android app.

## Overview

The Vibestempel app uses Supabase for:
- **PostgreSQL Database**: Store users, events, and stamps
- **Realtime**: Live updates for admin dashboard
- **Row Level Security (RLS)**: Secure data access
- **RESTful API**: Auto-generated from database schema

## Prerequisites

1. A Supabase account (sign up at https://supabase.com)
2. Android Studio with the project opened
3. Basic understanding of databases and SQL
4. The Vibestempel source code

---

## Step 1: Create a Supabase Project

1. **Sign in** to https://supabase.com
2. Click **"New Project"**
3. Enter project details:
   - **Name**: `vibestempel` (or your preferred name)
   - **Database Password**: Choose a strong password
     - ⚠️ **Save this password securely!** You'll need it for database access
   - **Region**: Choose the region closest to your users (e.g., Europe - Frankfurt for German users)
   - **Pricing Plan**: Free tier works for development and small deployments
4. Click **"Create new project"**
5. Wait 2-3 minutes for project initialization

**What happens:** Supabase creates a dedicated PostgreSQL database, sets up authentication, and generates API endpoints.

---

## Step 2: Set Up Database Schema

### Import the Schema

1. In your Supabase dashboard, navigate to **SQL Editor** (left sidebar)
2. Open the file `supabase/schema.sql` from this repository
3. Copy the **entire contents** of the file
4. Paste into the SQL Editor in Supabase
5. Click **"Run"** (or press Ctrl/Cmd + Enter)

**Expected output:** You should see:
```
Success. No rows returned
```

### Verify Tables Were Created

1. Go to **Database** > **Tables** in the sidebar
2. Verify these tables exist:
   - ✅ **users** - Stores device-based user identification
   - ✅ **events** - Admin-created stamp collection events
   - ✅ **stamps** - User stamp collection records

3. Go to **Database** > **Views**
4. Verify this view exists:
   - ✅ **user_stamp_counts** - Aggregated statistics for admin dashboard

### Understanding the Schema

**users table:**
```sql
- id (UUID, primary key)
- device_id (TEXT, unique) - Android device ID
- username (TEXT, optional)
- created_at, updated_at (TIMESTAMP)
```

**events table:**
```sql
- id (UUID, primary key)
- name (TEXT) - Event name shown in QR code
- description (TEXT) - Event details
- created_by (TEXT) - Admin identifier
- is_active (BOOLEAN) - Whether event is currently active
- created_at (TIMESTAMP)
```

**stamps table:**
```sql
- id (UUID, primary key)
- user_id (UUID, foreign key to users)
- event_id (UUID, foreign key to events)
- event_name (TEXT) - Cached event name
- collected_at (TIMESTAMP)
- UNIQUE constraint on (user_id, event_id) - Prevents duplicates
```

---

## Step 3: Get Your Supabase Credentials

1. In your Supabase dashboard, go to **Settings** > **API** (left sidebar)
2. Find and copy these values:

   **Project URL:**
   ```
   https://xxxxxxxxxxxxx.supabase.co
   ```

   **anon public key** (under "Project API keys"):
   ```
   eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFz...
   ```

**⚠️ Important Security Notes:**
- Use the **anon public** key (NOT the service_role key!)
- The anon key is safe for client-side use (it's limited by RLS policies)
- Never commit these credentials to version control
- The service_role key has full database access - keep it secret!

---

## Step 4: Configure the Android App

### Recommended Method: local.properties

1. In the project root directory, copy the example file:
   ```bash
   cp local.properties.example local.properties
   ```

2. Edit `local.properties` and add your Supabase credentials:
   ```properties
   # Supabase Configuration
   supabase.url=https://xxxxxxxxxxxxx.supabase.co
   supabase.key=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```

3. **Verify** the file is in `.gitignore` (it should be by default)

**Why this method?**
- Keeps credentials out of source code
- File is automatically ignored by git
- Easy to update without code changes
- Works with BuildConfig for compile-time injection

### Alternative Methods

**For CI/CD (GitHub Actions, etc.):**

Set environment variables:
```yaml
env:
  SUPABASE_URL: ${{ secrets.SUPABASE_URL }}
  SUPABASE_KEY: ${{ secrets.SUPABASE_KEY }}
```

**For production builds:**

Use gradle properties in `~/.gradle/gradle.properties`:
```properties
SUPABASE_URL=https://xxxxxxxxxxxxx.supabase.co
SUPABASE_KEY=your-anon-key
```

---

## Step 5: Enable Realtime

**This is critical for the admin dashboard realtime updates!**

1. In Supabase dashboard, go to **Database** > **Replication** (left sidebar)
2. You'll see a list of all tables
3. Enable replication for these tables by clicking the toggle:
   - ✅ **users**
   - ✅ **events**
   - ✅ **stamps**

**What this does:**
- Enables real-time subscriptions to table changes
- Powers the admin dashboard live user stamp counter
- Uses PostgreSQL's logical replication feature
- Low overhead, scales well

**Note:** The `user_stamp_counts` view doesn't need replication enabled.

---

## Step 6: Verify Row Level Security (RLS)

The schema automatically enables RLS. Let's verify it's set up correctly:

### Check RLS is Enabled

1. Go to **Database** > **Tables**
2. Click on each table (`users`, `events`, `stamps`)
3. Verify **RLS is enabled** (shown at top of table view)

### Verify Policies

1. Go to **Authentication** > **Policies** (left sidebar)
2. Check each table has the correct policies:

**users table** should have:
- ✅ "Users can read own data" (SELECT)
- ✅ "Users can insert own data" (INSERT)
- ✅ "Users can update own data" (UPDATE)

**events table** should have:
- ✅ "Everyone can read active events" (SELECT)
- ✅ "Admins can create events" (INSERT)
- ✅ "Admins can update events" (UPDATE)

**stamps table** should have:
- ✅ "Users can read own stamps" (SELECT)
- ✅ "Users can insert own stamps" (INSERT)
- ✅ "Admins can read all stamps" (SELECT)

**Why RLS matters:**
- Prevents unauthorized data access
- Enforces security at database level
- Users can't see other users' stamps
- Admins can view all data for the dashboard

---

## Step 7: Test Database Functions

The schema includes serverside functions for safe operations.

### Test get_or_create_user function

1. Go to **SQL Editor**
2. Run this test:
   ```sql
   SELECT get_or_create_user('test-device-123', 'Test User');
   ```
3. Expected: Returns a UUID (user ID)
4. Run again - should return the **same** UUID (user already exists)

### Test add_stamp function

1. First, create a test event:
   ```sql
   INSERT INTO events (id, name, description, is_active)
   VALUES (
     'test-event-uuid',
     'Test Event',
     'Testing stamp collection',
     true
   );
   ```

2. Then test adding a stamp:
   ```sql
   SELECT add_stamp(
     'test-device-123',
     'test-event-uuid',
     'Test Event'
   );
   ```

3. Expected result:
   ```json
   {"success": true, "message": "Stamp collected successfully"}
   ```

4. Try again - should return:
   ```json
   {"success": false, "message": "Stamp already collected for this event"}
   ```

**Clean up test data:**
```sql
DELETE FROM stamps WHERE event_id = 'test-event-uuid';
DELETE FROM events WHERE id = 'test-event-uuid';
DELETE FROM users WHERE device_id = 'test-device-123';
```

---

## Step 8: Test the Android App Integration

### Build and Run

1. Open the project in Android Studio
2. Sync Gradle files (File > Sync Project with Gradle Files)
3. Build the app:
   ```bash
   ./gradlew assembleDebug
   ```
4. Run on an emulator or device

### Test Admin Flow

1. Open the app
2. Select **"Admin Modus"**
3. Enter admin token (default: `admin123`)
4. Create a test event:
   - Event Name: "Test Event"
   - Description: "Testing Supabase"
5. Click **"QR-Code generieren"**

### Verify in Supabase

1. Go to Supabase **Table Editor** > **events**
2. You should see your new event
3. Note the `id` (UUID) - this is embedded in the QR code

### Test User Flow

1. In the app, go back and select **"Benutzer Modus"**
2. Click **"QR-Code scannen"**
3. Scan the QR code you just created
4. Should see celebration animation

### Verify Stamp in Supabase

1. Go to Supabase **Table Editor** > **stamps**
2. You should see a new stamp record
3. Check **users** table - should have auto-created user

### Test Realtime Admin Dashboard

1. Keep the app open in Admin mode
2. In another device/emulator, scan QR codes as a user
3. Watch the admin dashboard update in **real-time**
4. User stamp counts should increment immediately

**If realtime doesn't work:**
- Verify Realtime is enabled (Step 5)
- Check network connectivity
- Review Supabase logs (Settings > Logs)

---

## Step 9: Production Setup

### Secure the Admin Token

**⚠️ Critical:** Change the default admin token!

The default token `admin123` is documented and insecure.

**To change:**
1. In the app, the token is stored in SharedPreferences
2. First user to set a custom token: Opens Admin mode with default token
3. Future versions could add token management UI

**Alternative:** Implement proper admin authentication via Supabase Auth.

### Configure Backups

**Free tier:** 7-day backup retention
**Pro tier:** Point-in-time recovery

To download a backup:
```bash
pg_dump -h db.your-project.supabase.co \
  -U postgres \
  -d postgres \
  > backup.sql
```

### Monitor Usage

Check your Supabase dashboard regularly:
- **Database size**: Free tier has 500 MB limit
- **Bandwidth**: Free tier has 2 GB/month
- **API requests**: Monitor for unusual patterns
- **Realtime connections**: Should correlate with active users

---

## Troubleshooting

### Build Fails with "SUPABASE_URL not found"

**Cause:** Missing or incorrect `local.properties`

**Solution:**
1. Verify `local.properties` exists in project root
2. Check file contains correct key names
3. Rebuild: `./gradlew clean build`

### "Failed to create event" in app

**Possible causes:**
1. Wrong Supabase credentials
2. RLS policy blocking insert
3. Network connectivity

**Debug steps:**
1. Check Supabase logs (Settings > Logs)
2. Verify credentials in BuildConfig
3. Test direct API call with curl

### Realtime updates not working

**Checklist:**
- [ ] Realtime enabled for tables (Step 5)
- [ ] App has internet permission in AndroidManifest
- [ ] Supabase URL and key are correct
- [ ] Network allows WebSocket connections

### Stamps not appearing

**Check:**
1. RLS policies allow insert (see Step 6)
2. User ID is being created correctly
3. Event ID matches between QR code and database
4. Network connectivity

---

## Database Maintenance

### View Table Statistics

```sql
SELECT schemaname, tablename, 
       pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

### Check Realtime Connections

Go to **Database** > **Replication** to see:
- Active replication slots
- WAL (Write-Ahead Log) size
- Connection count

### Optimize Queries

The schema includes indexes on:
- `stamps(user_id)`
- `stamps(event_id)`
- `events(is_active)`

These optimize common queries used by the app.

---

## Next Steps

After setup is complete:

1. **Read** [DEPLOYMENT.md](../DEPLOYMENT.md) for deployment options
2. **Review** [ADMIN_DOCUMENTATION.md](../ADMIN_DOCUMENTATION.md) for admin features
3. **Test** thoroughly before production deployment
4. **Monitor** Supabase dashboard regularly
5. **Backup** your database regularly

---

## Support Resources

- 📚 **Supabase Documentation**: https://supabase.com/docs
- 💬 **Supabase Discord**: https://discord.supabase.com
- 🎮 **Android Docs**: https://developer.android.com
- 📖 **This Project**: [README.md](../README.md)

For Supabase-specific issues, the Supabase Discord community is very responsive.

---

**Last Updated:** January 2026  
**Version:** 2.0 (Direct Supabase Integration)  
**Tested with:** Supabase v2.0, Android SDK 34

The policies ensure:
- Users can only read/write their own stamps
- Everyone can read active events
- Admins can create/modify events
- Admins can view all users' stamps

## Step 7: Test the Setup

1. Build and run the Android app
2. Create a test event in Admin mode
3. Scan the QR code in User mode
4. Check Supabase dashboard > **Table Editor** > `stamps` to see the new entry
5. Verify realtime updates work in Admin dashboard

## Database Schema Overview

### Tables

**users**
- Stores device-based user identification
- Each device gets a unique user record
- Optional username field for future enhancements

**events**
- Stores admin-created events
- Each event has a unique UUID
- Can be marked active/inactive

**stamps**
- Links users to events (many-to-many)
- Prevents duplicate stamps via unique constraint
- Tracks collection timestamp

**user_stamp_counts** (VIEW)
- Aggregates stamp counts per user
- Used by admin dashboard for realtime monitoring

### Functions

**get_or_create_user(device_id, username)**
- Gets existing user or creates new one
- Used when collecting stamps

**add_stamp(device_id, event_id, event_name)**
- Safely adds a stamp with duplicate checking
- Returns success/failure status
- Used by the app when scanning QR codes

## Security Best Practices

1. **Never commit Supabase credentials** to version control
2. Use **Row Level Security (RLS)** policies to protect data
3. Store credentials in:
   - `local.properties` (local development)
   - GitHub Secrets (CI/CD)
   - Environment variables (production)
4. Regularly rotate your Supabase **service_role** key
5. Use **anon** key in the app (never service_role)

## Troubleshooting

### "Failed to connect to Supabase"
- Verify your URL and key are correct
- Check internet connectivity
- Ensure Supabase project is active

### "Permission denied" errors
- Verify RLS policies are set up correctly
- Check that realtime is enabled for the tables

### Stamps not appearing in realtime
- Ensure realtime replication is enabled
- Check that the admin is subscribed to the correct table/channel

### App crashes on stamp collection
- Check Supabase logs in dashboard
- Verify the schema was applied correctly
- Ensure device has internet connection

## Next Steps

After setup:
1. Test the full flow: Admin creates event → User scans → Admin sees count update
2. Monitor Supabase dashboard for activity
3. Set up authentication (for future enhanced security)
4. Consider implementing time-limited events
5. Add user profiles and gamification features

## Migration from Local Storage

If you have existing users with local stamps:
1. Users will need to re-collect stamps (serverside starts fresh)
2. Alternative: Create a migration script to upload local stamps to Supabase
3. Use device_id to link old stamps to new user records

## Support

For Supabase-specific issues:
- Supabase Documentation: https://supabase.com/docs
- Supabase Discord: https://discord.supabase.com

For app-specific issues:
- See main README.md
- Check ARCHITECTURE.md for technical details
