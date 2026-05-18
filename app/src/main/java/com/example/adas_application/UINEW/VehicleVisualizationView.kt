package com.example.adas_application.UINEW

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import com.example.adas_application.mock.DetectedObject

// custom View that draws the 3D visualization on screen
// Canvas is Android's built-in drawing tool
// think of it like a whiteboard — we draw shapes on it
// every time new data comes in — we redraw everything
class VehicleVisualizationView(context: Context) : View(context) {

    // current list of detected objects to draw
    // updated every 1 second by ViewModel
    private var detectedObjects: List<DetectedObject> = emptyList()

    // paint = like a marker/brush
    // each paint has its own color, thickness, style

    // dark green grid lines
    private val gridPaint = Paint().apply {
        color = Color.parseColor("#1f3a1f")
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }

    // boxes around detected objects
    // color changes per object type
    private val boxPaint = Paint().apply {
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    // text labels above each box — "CAR 14m"
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 24f
    }

    // green dashed line showing predicted path
    private val pathPaint = Paint().apply {
        color = Color.parseColor("#4ade80")
        strokeWidth = 3f
        style = Paint.Style.STROKE
        // dashed effect — 10px dash, 8px gap
        pathEffect = DashPathEffect(floatArrayOf(10f, 8f), 0f)
    }

    // green triangle — the ego vehicle (your car)
    private val egoPaint = Paint().apply {
        color = Color.parseColor("#4ade80")
        style = Paint.Style.FILL
        alpha = 200
    }

    // faint green lines — LiDAR sweep
    private val lidarPaint = Paint().apply {
        color = Color.parseColor("#4ade80")
        strokeWidth = 1f
        alpha = 40
    }

    // called by ViewModel when new objects arrive
    // invalidate() tells Android — redraw the canvas NOW
    fun updateObjects(objects: List<DetectedObject>) {
        detectedObjects = objects
        invalidate() // triggers onDraw() automatically
    }

    // Android calls this automatically whenever screen needs drawing
    // we never call this ourselves
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // fill background black
        canvas.drawColor(Color.parseColor("#0d0d0d"))

        // draw in order — back to front
        drawGrid(canvas)           // 1. grid lines first
        drawLidarSweeps(canvas)    // 2. LiDAR lines
        drawDetectedObjects(canvas) // 3. object boxes
        drawPredictedPath(canvas)  // 4. path line
        drawEgoVehicle(canvas)     // 5. ego vehicle last
    }

    // draws the perspective grid
    // makes it look like a 3D ground plane
    private fun drawGrid(canvas: Canvas) {

        // vanishing point = center top of view
        val vpX = width / 2f
        val vpY = height * 0.35f

        // horizontal lines at different heights
        listOf(0.85f, 0.75f, 0.65f, 0.57f, 0.50f).forEach { ratio ->
            val y = height * ratio
            canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
        }

        // lines from vanishing point to bottom edge
        // creates perspective effect
        listOf(0f, 0.15f, 0.3f, 0.45f, 0.5f, 0.55f, 0.7f, 0.85f, 1f).forEach { ratio ->
            canvas.drawLine(
                vpX, vpY,
                width * ratio, height.toFloat(),
                gridPaint
            )
        }
    }

    // draws faint LiDAR sweep lines from ego vehicle
    // shows the car is actively scanning the environment
    private fun drawLidarSweeps(canvas: Canvas) {
        val egoX = width / 2f
        val egoY = height * 0.88f

        listOf(
            Pair(width * 0.25f, height * 0.70f),
            Pair(width * 0.35f, height * 0.65f),
            Pair(width * 0.65f, height * 0.68f),
            Pair(width * 0.75f, height * 0.72f)
        ).forEach { (x, y) ->
            canvas.drawLine(egoX, egoY, x, y, lidarPaint)
        }
    }

    // draws bounding boxes around detected objects
    // different color per object type
    private fun drawDetectedObjects(canvas: Canvas) {

        // color map — each object type gets its own color
        val colors = mapOf(
            "CAR" to Color.parseColor("#22d3ee"),   // cyan
            "VEH" to Color.parseColor("#facc15"),   // yellow
            "PED" to Color.parseColor("#a78bfa"),   // purple
            "TRUCK" to Color.parseColor("#f97316")  // orange
        )

        detectedObjects.forEach { obj ->

            // set color for this object type
            val color = colors[obj.type] ?: Color.WHITE
            boxPaint.color = color
            textPaint.color = color
            textPaint.textSize = 24f

            // draw rectangle around detected object
            canvas.drawRect(
                obj.x,          // left
                obj.y,          // top
                obj.x + 80f,    // right
                obj.y + 45f,    // bottom
                boxPaint
            )

            // draw label above the box
            // shows type and distance — "CAR 14m"
            canvas.drawText(
                "${obj.type} ${obj.distance}m",
                obj.x,
                obj.y - 8f,
                textPaint
            )
        }
    }

    // draws the predicted path as a dashed green curve
    // shows where the car is going
    private fun drawPredictedPath(canvas: Canvas) {
        val path = Path()
        val startX = width / 2f
        val startY = height * 0.90f

        // curved line going forward — quadratic bezier curve
        path.moveTo(startX, startY)
        path.quadTo(
            startX + 15f, height * 0.65f,  // control point
            startX + 25f, height * 0.35f   // end point
        )
        canvas.drawPath(path, pathPaint)
    }

    // draws a green triangle at the bottom center
    // represents our car — the ego vehicle
    private fun drawEgoVehicle(canvas: Canvas) {
        val cx = width / 2f
        val cy = height * 0.88f

        val path = Path()
        path.moveTo(cx, cy - 20f)        // top point
        path.lineTo(cx - 15f, cy + 10f)  // bottom left
        path.lineTo(cx + 15f, cy + 10f)  // bottom right
        path.close()

        canvas.drawPath(path, egoPaint)
    }
}