package com.example.adas_application.mock

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MockVehicleSignalManager {

    val speedSignal: Flow<Float> = flow {
        while (true) {
            val fakeSpeed = (55..80).random().toFloat()
            emit(fakeSpeed)
            delay(2000)
        }
    }
    val detectedObjects: Flow<List<DetectedObject>> = flow {
        while (true) {
            val objects = listOf(
                DetectedObject(
                    type = "CAR",
                    x = (200..350).random().toFloat(),
                    y = (140..170).random().toFloat(),
                    distance = (10..20).random()
                ),
                DetectedObject(
                    type = "VEH",
                    x = (380..470).random().toFloat(),
                    y = (145..175).random().toFloat(),
                    distance = (20..35).random()
                ),
                DetectedObject(
                    type = "PED",
                    x = (100..190).random().toFloat(),
                    y = (165..190).random().toFloat(),
                    distance = (25..45).random()
                )
            )
            // send the list to ViewModel
            emit(objects)
            // update every 1 second
            // faster than speed signal — objects move more frequently
            delay(1000)
        }
    }
}
// data class representing one detected object
// x, y = position on the visualization screen
// distance = how far away in meters
// type = what kind of object — CAR, VEH, PED, TRUCK
data class DetectedObject(
    val type: String,       // CAR, VEH, PED, TRUCK
    val x: Float,           // x position on canvas
    val y: Float,           // y position on canvas
    val distance: Int       // distance in meters from ego vehicle
)