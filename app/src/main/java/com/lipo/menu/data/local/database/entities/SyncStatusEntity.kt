package com.lipo.menu.data.local.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_status")
data class SyncStatusEntity(
    @PrimaryKey
    @ColumnInfo(name = "entity_id")
    val entityId: String,

    @ColumnInfo(name = "entity_type")
    val entityType: String, // "dish", "menu", "combination"

    @ColumnInfo(name = "sync_status")
    val syncStatus: String, // "SYNCED", "PENDING", "CONFLICT"

    @ColumnInfo(name = "last_sync_at")
    val lastSyncAt: Long?,

    @ColumnInfo(name = "local_updated_at")
    val localUpdatedAt: Long,

    @ColumnInfo(name = "remote_updated_at")
    val remoteUpdatedAt: Long?
)
