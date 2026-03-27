package com.lipo.menu.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lipo.menu.data.local.database.entities.SyncStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncDao {
    @Query("SELECT * FROM sync_status WHERE entity_id = :entityId")
    fun getSyncStatus(entityId: String): Flow<SyncStatusEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSyncStatus(syncStatus: SyncStatusEntity)

    @Query("SELECT * FROM sync_status WHERE sync_status = 'PENDING'")
    fun getPendingSyncEntities(): Flow<List<SyncStatusEntity>>

    @Query("DELETE FROM sync_status WHERE entity_id = :entityId")
    suspend fun deleteSyncStatus(entityId: String)
}
