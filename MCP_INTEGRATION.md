# Vibestempel - Supabase MCP Server Integration

## Overview

Vibestempel now uses a **Model Context Protocol (MCP) server** to communicate with Supabase for maximum security and realtime capabilities. All database operations are handled server-side, ensuring credentials never exist in the Android app.

## Architecture

```
┌─────────────────────┐
│   Android App       │
│  (Vibestempel)      │
└──────────┬──────────┘
           │ HTTP/WebSocket
           ▼
┌─────────────────────┐
│   MCP Server        │
│  (Node.js/Express)  │
└──────────┬──────────┘
           │ Supabase SDK
           ▼
┌─────────────────────┐
│   Supabase          │
│  (PostgreSQL +      │
│   Realtime)         │
└─────────────────────┘
```

## Key Features

✅ **Serverside Security**: Supabase credentials never exposed in the app
✅ **Realtime Updates**: Admin dashboard shows live user stamp counts
✅ **MCP Protocol**: Standardized API communication
✅ **Duplicate Prevention**: Server-side validation
✅ **User Tracking**: Device-based user identification

## Setup Instructions

### 1. Set Up Supabase

Follow the instructions in [`supabase/SETUP.md`](../supabase/SETUP.md) to:
- Create a Supabase project
- Run the database schema
- Get your credentials

### 2. Deploy the MCP Server

#### Option A: Local Development

```bash
cd .github/mcp
npm install
npm start
```

Set environment variables:
```bash
export SUPABASE_URL="https://your-project.supabase.co"
export SUPABASE_KEY="your-anon-key"
export PORT=3000
```

#### Option B: Docker

```bash
cd .github/mcp
docker build -t vibestempel-mcp .
docker run -p 3000:3000 \
  -e SUPABASE_URL="https://your-project.supabase.co" \
  -e SUPABASE_KEY="your-anon-key" \
  vibestempel-mcp
```

#### Option C: Cloud Deployment

Deploy to your preferred platform:

**Google Cloud Run:**
```bash
gcloud run deploy vibestempel-mcp \
  --source . \
  --set-env-vars SUPABASE_URL=<url>,SUPABASE_KEY=<key>
```

**Heroku:**
```bash
heroku create vibestempel-mcp
heroku config:set SUPABASE_URL=<url>
heroku config:set SUPABASE_KEY=<key>
git push heroku main
```

### 3. Configure the Android App

Update the MCP server URL in `MCPClient.kt`:

```kotlin
private const val MCP_SERVER_URL = "https://your-mcp-server.com/mcp"
```

For production, use BuildConfig or resources:

```kotlin
private val MCP_SERVER_URL = BuildConfig.MCP_SERVER_URL
```

### 4. Build and Run

```bash
./gradlew assembleDebug
```

## API Endpoints

The MCP server exposes a single endpoint: `POST /mcp`

### Request Format

```json
{
  "method": "query|insert|update|rpc|subscribe",
  "params": { ... }
}
```

### Methods

#### Query
```json
{
  "method": "query",
  "params": {
    "table": "stamps",
    "select": "*",
    "filters": { "user_id": "..." },
    "orderBy": "created_at.desc"
  }
}
```

#### Insert
```json
{
  "method": "insert",
  "params": {
    "table": "events",
    "data": { "name": "...", "description": "..." }
  }
}
```

#### RPC (Call Function)
```json
{
  "method": "rpc",
  "params": {
    "function": "add_stamp",
    "args": {
      "p_device_id": "...",
      "p_event_id": "...",
      "p_event_name": "..."
    }
  }
}
```

## Realtime Updates

The admin dashboard subscribes to realtime updates via WebSocket:

1. Connect to MCP server WebSocket
2. Subscribe to `stamps` table changes
3. Receive instant notifications when users collect stamps
4. UI updates automatically with new counts

## Security

### What's Protected ✅

- ✅ Supabase credentials stay on server
- ✅ Row Level Security policies enforced
- ✅ Server-side validation of all operations
- ✅ Duplicate stamp prevention at database level
- ✅ HTTPS encryption in production

### Best Practices

1. **Never commit credentials** to git
2. **Use environment variables** for all secrets
3. **Deploy MCP server** with HTTPS
4. **Enable rate limiting** on the MCP server
5. **Monitor server logs** for suspicious activity

## Monitoring

### Health Check

```bash
curl https://your-mcp-server.com/health
```

Response:
```json
{
  "status": "ok",
  "timestamp": "2024-01-14T12:00:00.000Z"
}
```

### Logs

Check MCP server logs:
```bash
# Docker
docker logs <container-id>

# PM2
pm2 logs vibestempel-mcp

# Cloud Run
gcloud logging read "resource.type=cloud_run_revision"
```

## Troubleshooting

### App can't connect to MCP server

**Problem**: "Failed to create event" or "Failed to collect stamp"

**Solutions**:
1. Verify MCP server is running: `curl http://localhost:3000/health`
2. Check MCP_SERVER_URL in `MCPClient.kt`
3. Ensure network permissions in AndroidManifest.xml
4. For localhost testing, use 10.0.2.2 on Android emulator

### Realtime updates not working

**Problem**: Admin dashboard doesn't update live

**Solutions**:
1. Verify Realtime is enabled in Supabase dashboard
2. Check WebSocket connection in browser dev tools
3. Ensure stamps table is added to replication
4. Review MCP server logs for errors

### CORS errors

**Problem**: Browser blocking requests

**Solution**: MCP server already has CORS enabled. For custom domains, update CORS config in `server.js`

## Development

### Running Tests

```bash
# Start MCP server
cd .github/mcp
npm start

# In another terminal, test endpoints
curl -X POST http://localhost:3000/mcp \
  -H "Content-Type: application/json" \
  -d '{"method":"query","params":{"table":"events"}}'
```

### Adding New Endpoints

1. Add method handler in `server.js`
2. Update `MCPClient.kt` with new method
3. Update `MCPStorage.kt` to use new method
4. Test thoroughly

## Migration from Local Storage

If migrating from the old `StempelStorage` (SharedPreferences):

1. Users will start fresh with MCP backend
2. Old local stamps are not automatically migrated
3. To migrate: create a script to upload local stamps to Supabase
4. Use device_id to link old stamps to new user records

## Future Enhancements

- [ ] Add authentication (user accounts)
- [ ] Implement rate limiting
- [ ] Add caching layer (Redis)
- [ ] Support offline mode with sync
- [ ] Add analytics and metrics
- [ ] Implement push notifications

## Support

For issues:
- Supabase: https://supabase.com/docs
- MCP Protocol: https://modelcontextprotocol.io
- App Issues: See main README.md

## License

This integration is part of the Vibestempel project for educational purposes.
