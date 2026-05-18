package com.example.adas_application
import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.adas_application.worker.AnnotationUploadWorker
import kotlinx.coroutines.flow.Flow

class AnnotationRepository(private val context: Context) {
    private val dao: AnnotationDao = AnnotationDatabase.getDatabase(context).annotationDao()
    private val firebaseRepository = FirebaseRepository()


    suspend fun saveAnnotation(
        sessionId: String,
        eventType: String,
        speed: Float,
        latitude: Double,
        longitude: Double
    ) {
        val annotation = Annotation(
            sessionId = sessionId,
            eventType = eventType,
            speed = speed,
            latitude = latitude,
            longitude = longitude
        )
        dao.insertAnnotation(annotation)
        firebaseRepository.uploadAnnotation(annotation)
        scheduleUpload()
    }

    fun getAnnotations(sessionId: String): Flow<List<Annotation>> {
        return dao.getAnnotationsBySession(sessionId)
    }

    private fun scheduleUpload() {
        // TODO: Implement AnnotationUploadWorker and uncomment this block

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
            
        val uploadRequest = OneTimeWorkRequestBuilder<AnnotationUploadWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueue(uploadRequest)

    }
}