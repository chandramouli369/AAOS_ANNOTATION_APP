package com.example.adas_application.mock

import android.car.Car
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.content.Context
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class RealVehicleSignalManager(private val context: Context) {
    private var car: Car? = null
    private var carPropertyManager: CarPropertyManager? = null
    private var isConnected = false

    fun connect(onConnected: () -> Unit, onDisconnected: () -> Unit) {
        Log.d("VHAL", "Trying to connect to Carservice...")
        car = Car.createCar(
            context,
            null,
            Car.CAR_WAIT_TIMEOUT_WAIT_FOREVER,
            object : Car.CarServiceLifecycleListener {
                override fun onLifecycleChanged(car: Car, ready: Boolean) {
                    if (ready) {
                        Log.d("VHAL", "CarService Connected!")
                        carPropertyManager = car.getCarManager(
                            Car.PROPERTY_SERVICE
                        ) as CarPropertyManager
                        isConnected = true
                        onConnected()
                    } else {
                        Log.d("VHAL", "Carservice disconnected!")
                        isConnected = false
                        onDisconnected()
                    }
                }
            }
        )
    }

    val speedSignal: Flow<Float> = callbackFlow {
        val cpm = carPropertyManager
        if (cpm == null) {
            Log.w("VHAL", "No CarPropertyManager — using fake speed")
            while (true) {
                trySend((55..80).random().toFloat())
                delay(2000)
            }
        }

        val callback = object : CarPropertyManager.CarPropertyEventCallback {
            override fun onChangeEvent(value: CarPropertyValue<*>?) {
                val speed = (value?.value as? Float) ?: 0f
                val speedMph = speed * 2.237f
                Log.d("VHAL", "Speed received: $speedMph mph")
                trySend(speedMph)
            }

            override fun onErrorEvent(p0: Int, p1: Int) {
                Log.e("VHAL", "Speed error!")
            }
        }

        cpm.registerCallback(
            callback,
            android.car.VehiclePropertyIds.PERF_VEHICLE_SPEED,
            CarPropertyManager.SENSOR_RATE_NORMAL
        )

        Log.d("VHAL", "Speed callback registered!")

        awaitClose {
            cpm.unregisterCallback(callback)
            Log.d("VHAL", "Speed callback removed")
        }
    }

    val detectedObjects: Flow<List<DetectedObject>> = callbackFlow {
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
            trySend(objects)
            delay(1000)
        }
    }

    fun disconnect(){
        car?.disconnect()
        car=null
        carPropertyManager=null
        isConnected=false
        Log.d("VHAL", "Disconnected from CarService")
    }
}
