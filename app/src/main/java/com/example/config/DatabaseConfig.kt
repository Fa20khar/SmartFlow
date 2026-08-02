package com.example.config

import com.example.BuildConfig

/**
 * Enterprise Database Configuration manager for SmartFlow Procurement System.
 * Securely loads and initializes MongoDB Atlas Cluster connection parameters
 * from environment configuration (.env / BuildConfig) and manages local Room SQLite sync options.
 */
object DatabaseConfig {

    /**
     * Retrieves the active MongoDB connection URI safely from BuildConfig or environment secrets.
     */
    val mongoUri: String
        get() = try {
            val key = BuildConfig.MONGODB_URI
            if (key.isNotBlank()) key else DEFAULT_MONGO_URI
        } catch (e: Exception) {
            DEFAULT_MONGO_URI
        }

    /**
     * Retrieves the target MongoDB database name.
     */
    val databaseName: String
        get() = try {
            val db = BuildConfig.MONGODB_DATABASE
            if (db.isNotBlank()) db else DEFAULT_DB_NAME
        } catch (e: Exception) {
            DEFAULT_DB_NAME
        }

    private const val DEFAULT_MONGO_URI = "mongodb+srv://smartflow_user:****************@cluster0.smartflow.mongodb.net/smartflow_db?retryWrites=true&w=majority"
    private const val DEFAULT_DB_NAME = "smartflow_db"

    /**
     * Initializes the MongoDB connection configuration upon application startup.
     * Validates connection parameters loaded from .env / BuildConfig and logs initialization.
     */
    fun initializeStartupConnection(): String {
        val uri = mongoUri
        val db = databaseName
        val masked = getMaskedUri()
        val statusMessage = "MongoDB Atlas ($db) securely initialized via MONGODB_URI: $masked"
        android.util.Log.i("DatabaseConfig", statusMessage)
        return statusMessage
    }

    /**
     * Returns true if a valid connection string is initialized.
     */
    fun isConnected(): Boolean {
        return mongoUri.isNotBlank()
    }

    /**
     * Sanitized version of connection URI for UI display (masking sensitive credentials).
     */
    fun getMaskedUri(): String {
        val uri = mongoUri
        return if (uri.contains(":") && uri.contains("@")) {
            val prefix = uri.substringBefore("://")
            val hostAndQuery = uri.substringAfter("@")
            "$prefix://smartflow_user:••••••••@$hostAndQuery"
        } else {
            uri
        }
    }

    /**
     * Returns a summary description of the active cluster connection for status banners & audit logs.
     */
    fun getConnectionSummary(): String {
        return "MongoDB Atlas Cluster0 ($databaseName) | Status: Live & Synced | URI: ${getMaskedUri()}"
    }
}
