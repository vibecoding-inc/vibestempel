# Vibestempel MCP Server Configuration

This directory contains the Model Context Protocol (MCP) server configuration for Vibestempel's Supabase backend.

## What is MCP?

Model Context Protocol (MCP) is a standardized way for applications to communicate with backend services. For Vibestempel, we use the Supabase MCP server to handle all database operations.

## Configuration Files

- `supabase.json` - MCP server configuration for Supabase
- `server-config.json` - Main MCP server configuration

## Setup

### 1. Install MCP Server

The Supabase MCP server is automatically installed via npx. No manual installation required.

### 2. Set Environment Variables

Create a `.env` file in the project root or set environment variables:

```bash
export SUPABASE_URL="https://your-project.supabase.co"
export SUPABASE_KEY="your-anon-key-here"
```

### 3. Running the MCP Server

For development and testing:

```bash
# From the project root
npx @modelcontextprotocol/server-supabase
```

The server will:
- Connect to your Supabase instance
- Expose database operations via MCP protocol
- Handle realtime subscriptions
- Manage authentication and security

### 4. MCP Server Endpoints

The MCP server provides these capabilities:

- **Query Database**: Execute SQL queries
- **Insert Data**: Add records to tables
- **Update Data**: Modify existing records
- **Delete Data**: Remove records
- **Subscribe to Changes**: Realtime updates via WebSocket
- **Call Functions**: Execute Supabase database functions

## Integration with Android App

The Android app communicates with the MCP server via HTTP/WebSocket. The MCP server acts as a secure intermediary between the app and Supabase.

### Benefits of Using MCP Server

1. **Security**: Credentials stay on the server, not in the app
2. **Validation**: Server-side validation before database operations
3. **Abstraction**: App doesn't need direct Supabase SDK
4. **Flexibility**: Easy to switch backends without app changes
5. **Monitoring**: Centralized logging and monitoring

## Production Deployment

For production, deploy the MCP server as a standalone service:

### Option 1: Docker

```bash
docker build -t vibestempel-mcp .
docker run -e SUPABASE_URL=$SUPABASE_URL -e SUPABASE_KEY=$SUPABASE_KEY vibestempel-mcp
```

### Option 2: Cloud Service

Deploy to:
- Google Cloud Run
- AWS Lambda
- Heroku
- Vercel

### Option 3: Self-Hosted

Run on your own server with PM2:

```bash
npm install -g pm2
pm2 start "npx @modelcontextprotocol/server-supabase" --name vibestempel-mcp
```

## Security Considerations

1. **Never expose SUPABASE_KEY in the Android app**
2. **Use environment variables for all credentials**
3. **Implement rate limiting on the MCP server**
4. **Use HTTPS in production**
5. **Validate all inputs server-side**

## Monitoring

Monitor the MCP server:

```bash
# Check server logs
pm2 logs vibestempel-mcp

# Monitor performance
pm2 monit
```

## Troubleshooting

### Server won't start
- Check environment variables are set
- Verify Supabase credentials
- Ensure network connectivity

### Connection errors
- Verify MCP server URL in app configuration
- Check firewall settings
- Ensure server is running

### Realtime not working
- Verify Realtime is enabled in Supabase
- Check WebSocket connection
- Review server logs for errors

## Support

For MCP-specific issues:
- MCP Documentation: https://modelcontextprotocol.io
- Supabase MCP Server: https://github.com/modelcontextprotocol/servers

For app-specific issues:
- See main README.md
- Check supabase/SETUP.md
