package com.example.adas_application.UINEW

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.adas_application.Annotation
import com.example.adas_application.viewmodel.AnnotationViewModel

// main CID screen — everything the driver sees
// built with Jetpack Compose
// observes ViewModel state and reacts automatically
@Composable
fun CIDScreen(vm: AnnotationViewModel = viewModel()) {

    // collect all state from ViewModel
    // every time these change — UI redraws automatically
    val speed by vm.speed.collectAsState()
    val annotations by vm.annotations.collectAsState()
    val syncStatus by vm.syncStatus.collectAsState()
    val detectedObjects by vm.detectedObjects.collectAsState()
    val sessionActive by vm.sessionActive.collectAsState()

    // dropdown state
    var selectedEvent by remember { mutableStateOf("Lane drift") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    val eventTypes = listOf(
        "Lane drift",
        "Missed stop sign",
        "Unexpected braking",
        "Near collision",
        "Sensor anomaly",
        "Object detection failure"
    )

    // whenever detected objects update — tell canvas to redraw
    // We get the vizView from the factory now for better lifecycle management
    var vizViewRef by remember { mutableStateOf<VehicleVisualizationView?>(null) }

    LaunchedEffect(detectedObjects) {
        vizViewRef?.updateObjects(detectedObjects)
    }

    // full screen dark background
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {

        // ---- STATUS BAR ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF111111))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // session status indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (sessionActive) Color(0xFF4ADE80)
                            else Color(0xFFEF4444),
                            RoundedCornerShape(50)
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (sessionActive) "SESSION ACTIVE"
                    else "SESSION ENDED",
                    color = if (sessionActive) Color(0xFF4ADE80)
                    else Color(0xFFEF4444),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            // speed from mock VHAL
            Text(
                text = "${speed.toInt()} mph",
                color = Color(0xFF555555),
                fontSize = 11.sp
            )
        }

        // ---- VISUALIZATION PANEL ----
        // AndroidView embeds our custom Canvas View inside Compose
        // Canvas and Compose don't mix directly
        // AndroidView is the bridge between them
        AndroidView(
            factory = { ctx ->
                VehicleVisualizationView(ctx).also {
                    vizViewRef = it
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            update = {
                // updates can also happen here if needed
            }
        )

        // ---- SIGNAL STATUS ROW ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F0F0F))
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SignalPill("CarPropertyManager", "CONNECTED", Color(0xFF22D3EE))
            SignalPill("WorkManager", "ACTIVE", Color(0xFFA78BFA))
            SignalPill("Room DB", "READY", Color(0xFF4ADE80))
        }

        // ---- ANNOTATION CONTROLS ----
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF111111))
                .padding(16.dp)
        ) {
            // event type dropdown
            Box {
                OutlinedButton(
                    onClick = { dropdownExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFCCCCCC)
                    )
                ) {
                    Text(selectedEvent, fontSize = 12.sp)
                }
                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    eventTypes.forEach { event ->
                        DropdownMenuItem(
                            text = { Text(event) },
                            onClick = {
                                selectedEvent = event
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // main flag button
            Button(
                onClick = {
                    if (sessionActive) vm.onAnnotate(selectedEvent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF14532D)
                ),
                shape = RoundedCornerShape(10.dp),
                enabled = sessionActive
            ) {
                Text(
                    text = "🚩  Flag this moment",
                    color = Color(0xFF4ADE80),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // stop session button
            OutlinedButton(
                onClick = { vm.endSession() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF666666)
                )
            ) {
                Text(
                    text = if (sessionActive) "⏹ Stop session"
                    else "Session ended",
                    fontSize = 12.sp
                )
            }
        }

        // ---- ANNOTATIONS LIST ----
        // shows all annotations saved in Room DB
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF0A0A0A))
                .padding(16.dp)
        ) {
            Text(
                text = "ANNOTATIONS — ROOM DB",
                color = Color(0xFF333333),
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // list updates automatically when Room DB changes
            LazyColumn {
                items(annotations) { annotation ->
                    AnnotationRow(annotation = annotation)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // sync status row at bottom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // dot changes color — yellow pending, green synced
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                if (annotations.any {
                                        it.syncStatus == "PENDING"
                                    }) Color(0xFFFACC15)
                                else Color(0xFF4ADE80),
                                RoundedCornerShape(50)
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = syncStatus,
                        color = Color(0xFF555555),
                        fontSize = 11.sp
                    )
                }
                Text(
                    text = "${annotations.size} total",
                    color = Color(0xFF333333),
                    fontSize = 11.sp
                )
            }
        }
    }
}

// one row in the annotation list
// shows event type, speed, and sync status
@Composable
fun AnnotationRow(annotation: Annotation) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF111111), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = annotation.eventType,
                color = Color(0xFFCCCCCC),
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${annotation.speed.toInt()} mph",
                color = Color(0xFF444444),
                fontSize = 10.sp
            )
        }
        // yellow = pending, green = synced
        Text(
            text = if (annotation.syncStatus == "SYNCED") "Synced ✓"
            else "Pending ↑",
            color = if (annotation.syncStatus == "SYNCED")
                Color(0xFF4ADE80) else Color(0xFFFACC15),
            fontSize = 10.sp
        )
    }
}

// small signal status pill
@Composable
fun SignalPill(label: String, status: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .background(color, RoundedCornerShape(50))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = Color(0xFF444444),
            fontSize = 9.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = status,
            color = color,
            fontSize = 9.sp
        )
    }
}