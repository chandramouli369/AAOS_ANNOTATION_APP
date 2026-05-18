package com.example.adas_application.worker
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.adas_application.AnnotationDatabase
import kotlinx.coroutines.delay

class AnnotationUploadWorker(
    context: Context,
    params: WorkerParameters
): CoroutineWorker(context,params) {
    override suspend fun doWork(): Result {
        return try{
            val dao= AnnotationDatabase.getDatabase(applicationContext).annotationDao()
            val pendingAnnotations=dao.getPendingAnnotations()
            pendingAnnotations.forEach { annotation ->
                delay(2000)
                dao.updateSyncStatus(annotation.id,"SYNCED")
            }
            Result.success()
        } catch (e: Exception){
            Result.retry()
        }
    }
}