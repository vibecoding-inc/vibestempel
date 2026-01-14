# Vibestempel MCP Server Configuration

This directory contains the Model Context Protocol (MCP) server configuration for Vibestempel's Supabase backend.

## What is MCP?

Model Context Protocol (MCP) is a standardized way for applications to communicate with backend services. For Vibestempel, we use a custom MCP server that acts as a secure bridge between the Android app and Supabase.

## Files in This Directory

- **server.js** - Express.js server implementing MCP protocol
- **package.json** - Node.js dependencies
- **Dockerfile** - Container configuration for deployment
- **supabase.json** - MCP server configuration
- **README.md** - This file

## Quick Start

### Prerequisites

- Node.js 18+ installed
- Supabase project created and configured
- Environment variables set

### Installation

```bash
npm install
```

### Configuration

Set environment variables:

```bash
export SUPABASE_URL="https://your-project.supabase.co"
export SUPABASE_KEY="your-anon-key-here"
export PORT=3000  # Optional, defaults to 3000
```

### Running Locally

```bash
npm start
```

The server will start on `http://localhost:3000`.

Test it:
```bash
curl http://localhost:3000/health
```

Expected response:
```json
{
  "status": "ok",
  "timestamp": "2026-01-14T12:00:00.000Z"
}
```

## Deployment

### Option 1: Docker

Build the image:
```bash
docker build -t vibestempel-mcp .
```

Run the container:
```bash
docker run -p 3000:3000 \
  -e SUPABASE_URL="https://your-project.supabase.co" \
  -e SUPABASE_KEY="your-anon-key" \
  vibestempel-mcp
```

### Option 2: Google Cloud Run

```bash
gcloud run deploy vibestempel-mcp \
  --source . \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars SUPABASE_URL=<url>,SUPABASE_KEY=<key>
```

### Option 3: Heroku

```bash
heroku create vibestempel-mcp
heroku config:set SUPABASE_URL=<url>
heroku config:set SUPABASE_KEY=<key>
git push heroku main
```

### Option 4: PM2 (Self-Hosted)

```bash
npm install -g pm2
pm2 start npm --name "vibestempel-mcp" -- start
pm2 save
pm2 startup
```

## API Documentation

### Health Check

**GET /health**

Returns server status.

Response:
```json
{
  "status": "ok",
  "timestamp": "2026-01-14T12:00:00.000Z"
}
```

### MCP Endpoint

**POST /mcp**

Main endpoint for all database operations.

Request format:
```json
{
  "method": "query|insert|update|rpc|subscribe",
  "params": { ... }
}
```

#### Query Operation

Get data from a table.

```json
{
  "method": "query",
  "params": {
    "table": "events",
    "select": "*",
    "filters": { "is_active": true },
    "orderBy": "created_at.desc"
  }
}
```

Response:
```json
{
  "data": [...]
}
```

#### Insert Operation

Add a new record.

```json
{
  "method": "insert",
  "params": {
    "table": "events",
    "data": {
      "id": "uuid",
      "name": "Welcome Event",
      "description": "First event",
      "is_active": true
    }
  }
}
```

Response:
```json
{
  "data": { ... }
}
```

#### Update Operation

Modify existing records.

```json
{
  "method": "update",
  "params": {
    "table": "events",
    "data": { "is_active": false },
    "filters": { "id": "event-uuid" }
  }
}
```

Response:
```json
{
  "data": { ... }
}
```

#### RPC Operation

Call a Supabase function.

```json
{
  "method": "rpc",
  "params": {
    "function": "add_stamp",
    "args": {
      "p_device_id": "device-id",
      "p_event_id": "event-uuid",
      "p_event_name": "Event Name"
    }
  }
}
```

Response:
```json
{
  "data": {
    "success": true,
    "message": "Stamp collected successfully"
  }
}
```

#### Subscribe Operation

Subscribe to realtime updates (via WebSocket).

```json
{
  "method": "subscribe",
  "params": {
    "table": "stamps",
    "event": "*"
  }
}
```

Response:
```json
{
  "subscriptionId": "sub_123...",
  "message": "Use WebSocket for realtime updates"
}
```

### WebSocket API

Connect to `ws://your-server:3000` for realtime updates.

Send subscription request:
```json
{
  "action": "subscribe",
  "table": "stamps",
  "event": "*"
}
```

Receive updates:
```json
{
  "type": "update",
  "table": "stamps",
  "event": "INSERT",
  "data": { ... }
}
```

Unsubscribe:
```json
{
  "action": "unsubscribe",
  "table": "stamps"
}
```

## Security

### Environment Variables

**Never commit these to git!**

- `SUPABASE_URL` - Your Supabase project URL
- `SUPABASE_KEY` - Your Supabase anon/public key (NOT service_role!)
- `PORT` - Server port (optional)

### Best Practices

1. **Use HTTPS in production**
2. **Implement rate limiting** (add middleware)
3. **Monitor logs** for suspicious activity
4. **Use anon key**, not service_role key
5. **Enable CORS** only for trusted origins
6. **Validate all inputs** server-side
7. **Keep dependencies updated**

## Monitoring

### Logs

**Docker:**
```bash
docker logs <container-id>
```

**PM2:**
```bash
pm2 logs vibestempel-mcp
```

**Cloud Run:**
```bash
gcloud logging read "resource.type=cloud_run_revision"
```

### Metrics

Monitor:
- Request count
- Error rate
- Response time
- WebSocket connections
- Database query performance

## Troubleshooting

### Server won't start

**Check:**
- Node.js version (18+)
- Environment variables set
- Port not already in use
- Supabase credentials correct

### Connection errors

**Check:**
- Server is running
- Firewall allows connections
- Correct URL in Android app
- CORS configured correctly

### Realtime not working

**Check:**
- Realtime enabled in Supabase
- Tables added to replication
- WebSocket connection established
- Event subscriptions active

## Development

### Adding New Endpoints

1. Add handler function in `server.js`
2. Update method switch case
3. Test with curl or Postman
4. Update Android `MCPClient.kt`
5. Document in this README

### Testing

```bash
# Health check
curl http://localhost:3000/health

# Query test
curl -X POST http://localhost:3000/mcp \
  -H "Content-Type: application/json" \
  -d '{"method":"query","params":{"table":"events"}}'
```

## Support

For issues:
- Supabase: https://supabase.com/docs
- MCP Protocol: https://modelcontextprotocol.io
- App Issues: See main README.md

## License

Part of the Vibestempel project for educational purposes.

---

**Version:** 1.0
**Last Updated:** January 2026

