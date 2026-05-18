package com.example.adas_application

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseRepository {

    private val db = FirebaseFirestore.getInstance()

    fun uploadAnnotation(annotation: Annotation) {
        val data = hashMapOf(
            "id" to annotation.id,
            "sessionId" to annotation.sessionId,
            "eventType" to annotation.eventType,
            "timestamp" to annotation.timestamp,
            "speed" to annotation.speed,
            "latitude" to annotation.latitude,
            "longitude" to annotation.longitude,
            "syncStatus" to "SYNCED",
            "uploadedAt" to System.currentTimeMillis()
        )

        db.collection("annotations")
            .document(annotation.id)
            .set(data)
            .addOnSuccessListener {
                Log.d("Firebase", "Uploaded: ${annotation.eventType}")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed: ${e.message}")
            }
    }
}
