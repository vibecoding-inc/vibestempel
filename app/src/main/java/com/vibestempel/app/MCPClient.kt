package com.vibestempel.app

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * MCP Client for communicating with the Supabase MCP server
 * This provides a bridge between the Android app and Supabase via MCP protocol
 */
class MCPClient(private val context: Context) {
    
    companion object {
        private const val TAG = "MCPClient"
        // MCP server URL - should be configured per environment
        private const val MCP_SERVER_URL = "http://localhost:3000/mcp" // Update for production
    }
    
    /**
     * Execute a query on the MCP server
     */
    suspend fun query(
        table: String,
        select: String = "*",
        filters: Map<String, Any> = emptyMap(),
        orderBy: String? = null
    ): Result<JSONArray> = withContext(Dispatchers.IO) {
        try {
            val requestBody = JSONObject().apply {
                put("method", "query")
                put("params", JSONObject().apply {
                    put("table", table)
                    put("select", select)
                    if (filters.isNotEmpty()) {
                        put("filters", JSONObject(filters))
                    }
                    orderBy?.let { put("orderBy", it) }
                })
            }
            
            val response = executeRequest(requestBody)
            Result.success(response.getJSONArray("data"))
        } catch (e: Exception) {
            Log.e(TAG, "Query failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Insert data via MCP server
     */
    suspend fun insert(
        table: String,
        data: JSONObject
    ): Result<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val requestBody = JSONObject().apply {
                put("method", "insert")
                put("params", JSONObject().apply {
                    put("table", table)
                    put("data", data)
                })
            }
            
            val response = executeRequest(requestBody)
            Result.success(response.getJSONObject("data"))
        } catch (e: Exception) {
            Log.e(TAG, "Insert failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Update data via MCP server
     */
    suspend fun update(
        table: String,
        data: JSONObject,
        filters: Map<String, Any>
    ): Result<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val requestBody = JSONObject().apply {
                put("method", "update")
                put("params", JSONObject().apply {
                    put("table", table)
                    put("data", data)
                    put("filters", JSONObject(filters))
                })
            }
            
            val response = executeRequest(requestBody)
            Result.success(response.getJSONObject("data"))
        } catch (e: Exception) {
            Log.e(TAG, "Update failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Call a Supabase function via MCP server
     */
    suspend fun callFunction(
        functionName: String,
        params: Map<String, Any>
    ): Result<JSONObject> = withContext(Dispatchers.IO) {
        try {
            val requestBody = JSONObject().apply {
                put("method", "rpc")
                put("params", JSONObject().apply {
                    put("function", functionName)
                    put("args", JSONObject(params))
                })
            }
            
            val response = executeRequest(requestBody)
            Result.success(response.getJSONObject("data"))
        } catch (e: Exception) {
            Log.e(TAG, "Function call failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Subscribe to realtime updates via MCP server
     */
    suspend fun subscribe(
        table: String,
        event: String = "*",
        callback: (JSONObject) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val requestBody = JSONObject().apply {
                put("method", "subscribe")
                put("params", JSONObject().apply {
                    put("table", table)
                    put("event", event)
                })
            }
            
            val response = executeRequest(requestBody)
            val subscriptionId = response.getString("subscriptionId")
            
            // In a real implementation, you'd set up a WebSocket connection here
            // For now, we'll use polling as a fallback
            
            Result.success(subscriptionId)
        } catch (e: Exception) {
            Log.e(TAG, "Subscribe failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Execute HTTP request to MCP server
     */
    private fun executeRequest(requestBody: JSONObject): JSONObject {
        val url = URL(MCP_SERVER_URL)
        val connection = url.openConnection() as HttpURLConnection
        
        try {
            connection.apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                doInput = true
            }
            
            // Write request body
            connection.outputStream.use { output ->
                output.write(requestBody.toString().toByteArray())
            }
            
            // Read response
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { reader ->
                    val response = reader.readText()
                    return JSONObject(response)
                }
            } else {
                val errorStream = connection.errorStream?.bufferedReader()?.readText() 
                    ?: "Unknown error"
                throw Exception("HTTP $responseCode: $errorStream")
            }
        } finally {
            connection.disconnect()
        }
    }
}
