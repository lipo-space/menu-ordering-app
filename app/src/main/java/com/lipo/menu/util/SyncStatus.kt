package com.lipo.menu.util

enum class SyncStatus {
    SYNCED,      // All data synced
    SYNCING,     // Sync in progress
    OFFLINE,     // No network
    CONFLICT     // Conflict detected, needs resolution
}

enum class ConflictResolution {
    LOCAL_WINS,   // Keep local version
    REMOTE_WINS   // Use remote version
}
