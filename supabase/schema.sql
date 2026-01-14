-- Vibestempel Supabase Database Schema
-- This schema provides serverside storage for stamps with realtime capabilities

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table to track individual users
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    device_id TEXT UNIQUE NOT NULL,
    username TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Events table for admin-created events
CREATE TABLE IF NOT EXISTS events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    description TEXT,
    created_by TEXT, -- admin identifier
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    is_active BOOLEAN DEFAULT true
);

-- Stamps table to track user stamp collection
CREATE TABLE IF NOT EXISTS stamps (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    event_id UUID REFERENCES events(id) ON DELETE CASCADE,
    event_name TEXT NOT NULL,
    collected_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(user_id, event_id) -- Prevent duplicate stamps
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_stamps_user_id ON stamps(user_id);
CREATE INDEX IF NOT EXISTS idx_stamps_event_id ON stamps(event_id);
CREATE INDEX IF NOT EXISTS idx_events_active ON events(is_active);

-- Create a view for admin dashboard - user stamp counts
CREATE OR REPLACE VIEW user_stamp_counts AS
SELECT 
    u.id as user_id,
    u.device_id,
    u.username,
    COUNT(s.id) as stamp_count,
    MAX(s.collected_at) as last_stamp_collected
FROM users u
LEFT JOIN stamps s ON u.id = s.user_id
GROUP BY u.id, u.device_id, u.username
ORDER BY stamp_count DESC, last_stamp_collected DESC;

-- Enable Row Level Security (RLS)
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE events ENABLE ROW LEVEL SECURITY;
ALTER TABLE stamps ENABLE ROW LEVEL SECURITY;

-- RLS Policies for users table
-- Users can read their own data
CREATE POLICY "Users can read own data" ON users
    FOR SELECT
    USING (device_id = current_setting('app.device_id', true));

-- Users can insert their own data
CREATE POLICY "Users can insert own data" ON users
    FOR INSERT
    WITH CHECK (true);

-- Users can update their own data
CREATE POLICY "Users can update own data" ON users
    FOR UPDATE
    USING (device_id = current_setting('app.device_id', true));

-- RLS Policies for events table
-- Everyone can read active events
CREATE POLICY "Everyone can read active events" ON events
    FOR SELECT
    USING (is_active = true);

-- Admins can create events (requires admin role)
CREATE POLICY "Admins can create events" ON events
    FOR INSERT
    WITH CHECK (current_setting('app.is_admin', true) = 'true');

-- Admins can update events
CREATE POLICY "Admins can update events" ON events
    FOR UPDATE
    USING (current_setting('app.is_admin', true) = 'true');

-- RLS Policies for stamps table
-- Users can read their own stamps
CREATE POLICY "Users can read own stamps" ON stamps
    FOR SELECT
    USING (
        user_id IN (
            SELECT id FROM users WHERE device_id = current_setting('app.device_id', true)
        )
    );

-- Users can insert their own stamps
CREATE POLICY "Users can insert own stamps" ON stamps
    FOR INSERT
    WITH CHECK (
        user_id IN (
            SELECT id FROM users WHERE device_id = current_setting('app.device_id', true)
        )
    );

-- Admins can read all stamps
CREATE POLICY "Admins can read all stamps" ON stamps
    FOR SELECT
    USING (current_setting('app.is_admin', true) = 'true');

-- Enable realtime for tables
ALTER PUBLICATION supabase_realtime ADD TABLE users;
ALTER PUBLICATION supabase_realtime ADD TABLE events;
ALTER PUBLICATION supabase_realtime ADD TABLE stamps;

-- Function to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for users table
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Function to get or create user by device_id
CREATE OR REPLACE FUNCTION get_or_create_user(p_device_id TEXT, p_username TEXT DEFAULT NULL)
RETURNS UUID AS $$
DECLARE
    v_user_id UUID;
BEGIN
    -- Try to find existing user
    SELECT id INTO v_user_id
    FROM users
    WHERE device_id = p_device_id;
    
    -- If not found, create new user
    IF v_user_id IS NULL THEN
        INSERT INTO users (device_id, username)
        VALUES (p_device_id, p_username)
        RETURNING id INTO v_user_id;
    END IF;
    
    RETURN v_user_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to add a stamp (with duplicate checking)
CREATE OR REPLACE FUNCTION add_stamp(
    p_device_id TEXT,
    p_event_id UUID,
    p_event_name TEXT
)
RETURNS JSONB AS $$
DECLARE
    v_user_id UUID;
    v_stamp_id UUID;
    v_result JSONB;
BEGIN
    -- Get or create user
    v_user_id := get_or_create_user(p_device_id);
    
    -- Try to insert stamp (will fail if duplicate due to unique constraint)
    BEGIN
        INSERT INTO stamps (user_id, event_id, event_name)
        VALUES (v_user_id, p_event_id, p_event_name)
        RETURNING id INTO v_stamp_id;
        
        v_result := jsonb_build_object(
            'success', true,
            'stamp_id', v_stamp_id,
            'message', 'Stamp collected successfully'
        );
    EXCEPTION
        WHEN unique_violation THEN
            v_result := jsonb_build_object(
                'success', false,
                'message', 'Stamp already collected for this event'
            );
    END;
    
    RETURN v_result;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
