package com.example.adas_application

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnotationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnotation(annotation: Annotation): Long

    @Query("SELECT * FROM annotations WHERE sessionId = :sessionId ORDER BY timestamp")
    fun getAnnotationsBySession(sessionId: String): Flow<List<Annotation>>

    @Query("SELECT * FROM annotations WHERE syncStatus = 'PENDING'")
    suspend fun getPendingAnnotations(): List<Annotation>

    @Query("UPDATE annotations SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String): Int

    @Query("DELETE FROM annotations WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: String): Int
}
