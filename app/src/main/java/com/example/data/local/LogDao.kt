package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.OperationLog
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Query("SELECT * FROM operation_logs ORDER BY id DESC LIMIT 200")
    fun getAllLogs(): Flow<List<OperationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: OperationLog)

    @Query("DELETE FROM operation_logs")
    suspend fun clearLogs()
}
