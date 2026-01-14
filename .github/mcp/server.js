import express from 'express';
import cors from 'cors';
import { createClient } from '@supabase/supabase-js';
import { WebSocketServer } from 'ws';
import dotenv from 'dotenv';

dotenv.config();

const app = express();
const port = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());

// Initialize Supabase client
const supabase = createClient(
  process.env.SUPABASE_URL,
  process.env.SUPABASE_KEY
);

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// MCP endpoint - handles all database operations
app.post('/mcp', async (req, res) => {
  try {
    const { method, params } = req.body;
    
    switch (method) {
      case 'query':
        await handleQuery(params, res);
        break;
      case 'insert':
        await handleInsert(params, res);
        break;
      case 'update':
        await handleUpdate(params, res);
        break;
      case 'rpc':
        await handleRPC(params, res);
        break;
      case 'subscribe':
        await handleSubscribe(params, res);
        break;
      default:
        res.status(400).json({ error: 'Unknown method' });
    }
  } catch (error) {
    console.error('MCP Error:', error);
    res.status(500).json({ error: error.message });
  }
});

// Handle query operations
async function handleQuery(params, res) {
  const { table, select = '*', filters = {}, orderBy } = params;
  
  let query = supabase.from(table).select(select);
  
  // Apply filters
  for (const [key, value] of Object.entries(filters)) {
    query = query.eq(key, value);
  }
  
  // Apply ordering
  if (orderBy) {
    const [column, direction = 'asc'] = orderBy.split('.');
    query = query.order(column, { ascending: direction === 'asc' });
  }
  
  const { data, error } = await query;
  
  if (error) {
    throw error;
  }
  
  res.json({ data });
}

// Handle insert operations
async function handleInsert(params, res) {
  const { table, data } = params;
  
  const { data: inserted, error } = await supabase
    .from(table)
    .insert(data)
    .select()
    .single();
  
  if (error) {
    throw error;
  }
  
  res.json({ data: inserted });
}

// Handle update operations
async function handleUpdate(params, res) {
  const { table, data, filters } = params;
  
  let query = supabase.from(table).update(data);
  
  // Apply filters
  for (const [key, value] of Object.entries(filters)) {
    query = query.eq(key, value);
  }
  
  const { data: updated, error } = await query.select();
  
  if (error) {
    throw error;
  }
  
  res.json({ data: updated[0] || {} });
}

// Handle RPC (function calls)
async function handleRPC(params, res) {
  const { function: functionName, args = {} } = params;
  
  const { data, error } = await supabase.rpc(functionName, args);
  
  if (error) {
    throw error;
  }
  
  res.json({ data });
}

// Handle realtime subscriptions
async function handleSubscribe(params, res) {
  const { table, event = '*' } = params;
  
  // Generate subscription ID
  const subscriptionId = `sub_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  
  // For HTTP response, just return the subscription ID
  // Actual subscription will be handled via WebSocket
  res.json({ subscriptionId, message: 'Use WebSocket for realtime updates' });
}

// Start HTTP server
const server = app.listen(port, () => {
  console.log(`Vibestempel MCP Server running on port ${port}`);
  console.log(`Supabase URL: ${process.env.SUPABASE_URL}`);
});

// WebSocket server for realtime updates
const wss = new WebSocketServer({ server });

wss.on('connection', (ws) => {
  console.log('WebSocket client connected');
  
  const subscriptions = new Map();
  
  ws.on('message', async (message) => {
    try {
      const { action, table, event = '*' } = JSON.parse(message);
      
      if (action === 'subscribe') {
        // Create Supabase realtime subscription
        const channel = supabase
          .channel(`${table}-changes`)
          .on(
            'postgres_changes',
            { event, schema: 'public', table },
            (payload) => {
              ws.send(JSON.stringify({
                type: 'update',
                table,
                event: payload.eventType,
                data: payload.new || payload.old
              }));
            }
          )
          .subscribe();
        
        subscriptions.set(table, channel);
        
        ws.send(JSON.stringify({
          type: 'subscribed',
          table
        }));
      } else if (action === 'unsubscribe') {
        const channel = subscriptions.get(table);
        if (channel) {
          await supabase.removeChannel(channel);
          subscriptions.delete(table);
        }
      }
    } catch (error) {
      console.error('WebSocket error:', error);
      ws.send(JSON.stringify({ type: 'error', message: error.message }));
    }
  });
  
  ws.on('close', async () => {
    console.log('WebSocket client disconnected');
    // Clean up subscriptions
    for (const channel of subscriptions.values()) {
      await supabase.removeChannel(channel);
    }
  });
});

// Graceful shutdown
process.on('SIGTERM', () => {
  console.log('SIGTERM received, shutting down gracefully...');
  server.close(() => {
    console.log('Server closed');
    process.exit(0);
  });
});
