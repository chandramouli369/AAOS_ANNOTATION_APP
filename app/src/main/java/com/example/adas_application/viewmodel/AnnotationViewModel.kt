package com.example.adas_application.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.adas_application.mock.DetectedObject
import com.example.adas_application.Annotation
import com.example.adas_application.AnnotationRepository
import com.example.adas_application.mock.RealVehicleSignalManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class AnnotationViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val repository = AnnotationRepository(application)

    private val signalManager = RealVehicleSignalManager(application)
    val sessionId: String = UUID.randomUUID().toString()
    private val _speed = MutableStateFlow(0f)
    val speed: StateFlow<Float> = _speed.asStateFlow()
    private val _detectedObjects = MutableStateFlow<List<DetectedObject>>(emptyList())
    val detectedObjects: StateFlow<List<DetectedObject>> = _detectedObjects.asStateFlow()
    private val _annotations = MutableStateFlow<List<Annotation>>(emptyList())
    val annotations: StateFlow<List<Annotation>> = _annotations.asStateFlow()

    private val _syncStatus = MutableStateFlow("Ready — tap flag to annotate")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private val _sessionActive = MutableStateFlow(true)
    val sessionActive: StateFlow<Boolean> = _sessionActive.asStateFlow()
    init {
        signalManager.connect(
            onConnected = {
                Log.d("VHAL", "Starting real signals..")
                startSpeedSignal()
            },
            onDisconnected = {
                Log.d("VHAL", "CarService Lost")
            }
        )
        startObjectDetection()
        observeAnnotations()
    }
    private fun startSpeedSignal() {
        viewModelScope.launch {
            // viewModelScope = coroutine tied to ViewModel lifecycle
            // automatically cancelled when ViewModel is destroyed
            signalManager.speedSignal.collect { newSpeed ->
                _speed.value = newSpeed
            }
        }
    }
    private fun startObjectDetection() {
        viewModelScope.launch {
            signalManager.detectedObjects.collect { objects ->
                _detectedObjects.value = objects
            }
        }
    }
    private fun observeAnnotations() {
        viewModelScope.launch {
            repository.getAnnotations(sessionId).collect { list ->
                _annotations.value = list

                // update sync status message based on what's in DB
                val pending = list.count { it.syncStatus == "PENDING" }
                val synced = list.count { it.syncStatus == "SYNCED" }

                _syncStatus.value = when {
                    // no annotations yet
                    list.isEmpty() ->
                        "Ready — tap flag to annotate"

                    // some still uploading
                    pending > 0 ->
                        "$pending uploading, $synced synced to cloud"

                    // all uploaded
                    else ->
                        "${list.size} annotations synced to Rivian cloud ✓"
                }
            }
        }
    }
    fun onAnnotate(eventType: String) {
        viewModelScope.launch {

            // update status immediately so driver sees feedback
            _syncStatus.value = "Saving to Room DB..."

            // call repository — which saves to Room DB and schedules upload
            // this is all repository does from ViewModel's perspective
            // ViewModel doesn't know HOW it saves — just that it does
            repository.saveAnnotation(
                sessionId = sessionId,
                eventType = eventType,
                speed = _speed.value,       // current speed from VHAL signal
                latitude = 32.1234,         // mock GPS coordinates
                longitude = -81.4567        // in production from GPS sensor
            )
        }
    }
    fun endSession() {
        _sessionActive.value = false
        _syncStatus.value = "Session ended — all data saved to Room DB"
    }
    override fun onCleared() {
        super.onCleared()
        // disconnect cleanly when ViewModel destroyed
        signalManager.disconnect()
    }
}