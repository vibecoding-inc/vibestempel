# Supabase Setup Guide for Vibestempel

This guide will help you set up Supabase as the backend for the Vibestempel app.

## Prerequisites

1. A Supabase account (sign up at https://supabase.com)
2. Android Studio with the project opened
3. Basic understanding of Supabase

## Step 1: Create a Supabase Project

1. Go to https://supabase.com and sign in
2. Click "New Project"
3. Enter project details:
   - **Name**: vibestempel (or your preferred name)
   - **Database Password**: Choose a strong password (save this!)
   - **Region**: Choose closest to your users
4. Click "Create new project" and wait for setup to complete

## Step 2: Set Up Database Schema

1. In your Supabase project dashboard, go to the **SQL Editor**
2. Copy the entire contents of `supabase/schema.sql`
3. Paste it into the SQL Editor
4. Click **Run** to execute the schema
5. Verify tables were created by going to **Table Editor**

You should see:
- `users` table
- `events` table
- `stamps` table
- `user_stamp_counts` view

## Step 3: Get Your Supabase Credentials

1. In your Supabase dashboard, go to **Settings** > **API**
2. Copy the following values:
   - **Project URL** (e.g., `https://xxxxx.supabase.co`)
   - **anon public** key (under "Project API keys")

## Step 4: Configure the Android App

1. Open the file `app/src/main/res/values/strings.xml`
2. Add your Supabase credentials (or use environment variables):

```xml
<string name="supabase_url">YOUR_SUPABASE_URL_HERE</string>
<string name="supabase_key">YOUR_SUPABASE_ANON_KEY_HERE</string>
```

**⚠️ IMPORTANT**: For production, use environment variables or Android's BuildConfig to store credentials securely. Do NOT commit credentials to your repository!

### Alternative: Using local.properties (Recommended)

1. Create/edit `local.properties` in the project root:

```properties
supabase.url=YOUR_SUPABASE_URL_HERE
supabase.key=YOUR_SUPABASE_ANON_KEY_HERE
```

2. This file is already in `.gitignore`, so it won't be committed.

## Step 5: Enable Realtime

1. In Supabase dashboard, go to **Database** > **Replication**
2. Enable replication for the following tables:
   - ✅ `users`
   - ✅ `events`
   - ✅ `stamps`

This allows the admin dashboard to receive realtime updates.

## Step 6: Configure Row Level Security (Optional but Recommended)

The schema already includes RLS policies. To verify:

1. Go to **Authentication** > **Policies**
2. Verify policies exist for `users`, `events`, and `stamps` tables

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
