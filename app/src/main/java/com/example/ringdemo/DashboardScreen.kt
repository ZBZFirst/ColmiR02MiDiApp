package com.example.ringdemo

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment
import com.example.ringdemo.ui.theme.RingDemoTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

enum class ConnectionStatus {
    Disconnected, Scanning, Connecting, ConnectedIdle, Streaming, Error
}

data class DashboardState(
    val status: ConnectionStatus = ConnectionStatus.Disconnected,
    val deviceName: String = "Colmi R02",
    val deviceAddress: String? = null,
    val rotation: Vec3 = Vec3(0f, 0f, 0f),
    val quaternion: Quaternion = Quaternion.identity(),
    val rotationMode: String = "QUATERNION",
    val gyro: Vec3 = Vec3(0f, 0f, 0f),
    val rssi: Int = -100,
    val rssiNorm: Float = 0f,
    val packetRate: Double = 0.0,
    val isSoundEnabled: Boolean = false,
    val logs: List<String> = emptyList(),
    val smoothing: Float = 1.5f,
    val masterGain: Float = 1.0f,
    val xRange: ClosedFloatingPointRange<Float> = 120f..880f,
    val yRange: ClosedFloatingPointRange<Float> = 120f..880f,
    val zRange: ClosedFloatingPointRange<Float> = 120f..880f,
    val axisEnabled: Triple<Boolean, Boolean, Boolean> = Triple(true, true, true),
    val onToggleAxis: (Int, Boolean) -> Unit = { _, _ -> },
    val onUpdateRange: (Int, ClosedFloatingPointRange<Float>) -> Unit = { _, _ -> }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DashboardState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onResetOrigin: () -> Unit,
    onToggleRotationMode: () -> Unit,
    onToggleSound: () -> Unit,
    onUpdateSmoothing: (Float) -> Unit,
    onUpdateGain: (Float) -> Unit,
    onToggleAxis: (Int, Boolean) -> Unit,
    onUpdateRange: (Int, ClosedFloatingPointRange<Float>) -> Unit,
    onClearLogs: () -> Unit
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Midi Ring Dashboard", fontWeight = FontWeight.Bold) },
                actions = {
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Settings")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Clear Logs") },
                            onClick = {
                                onClearLogs()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, null) }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HeaderSection(state, onConnect, onDisconnect)

            VisualizerSection(state, onResetOrigin, onToggleRotationMode)

                ControlSection(
                    state,
                    onToggleSound,
                    onUpdateSmoothing,
                    onUpdateGain
                )

                MappingSection(state, onToggleAxis, onUpdateRange)

                DiagnosticsSection(state)
            }

            // Floating Scroll Controls
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
                    .alpha(0.6f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        scope.launch {
                            scrollState.animateScrollTo((scrollState.value - 400).coerceAtLeast(0))
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Scroll Up")
                }
                SmallFloatingActionButton(
                    onClick = {
                        scope.launch {
                            scrollState.animateScrollTo((scrollState.value + 400).coerceAtMost(scrollState.maxValue))
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Scroll Down")
                }
            }
        }
    }
}

@Composable
fun HeaderSection(
    state: DashboardState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.deviceName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                StatusChip(state.status)
            }

            MorphingActionButton(
                status = state.status,
                onConnect = onConnect,
                onDisconnect = onDisconnect
            )
        }
    }
}

@Composable
fun StatusChip(status: ConnectionStatus) {
    val color = when (status) {
        ConnectionStatus.Disconnected -> MaterialTheme.colorScheme.outline
        ConnectionStatus.Scanning -> MaterialTheme.colorScheme.tertiary
        ConnectionStatus.Connecting -> MaterialTheme.colorScheme.primary
        ConnectionStatus.ConnectedIdle -> MaterialTheme.colorScheme.secondary
        ConnectionStatus.Streaming -> Color(0xFF4CAF50) // Material Green
        ConnectionStatus.Error -> MaterialTheme.colorScheme.error
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = status.name.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MorphingActionButton(
    status: ConnectionStatus,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    val isError = status == ConnectionStatus.Error
    val isConnected = status != ConnectionStatus.Disconnected && !isError

    val shapePercent by animateFloatAsState(
        targetValue = if (isError) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    // A simple "burst" shape generator
    val burstShape = remember(shapePercent) {
        GenericShape { size: Size, _: LayoutDirection ->
            val points = 12
            val innerRadius = size.width * 0.4f
            val outerRadius = size.width * 0.5f
            val centerX = size.width / 2
            val centerY = size.height / 2

            // Interpolate between rounded rect and burst
            moveTo(centerX + outerRadius, centerY)
            for (i in 1..points * 2) {
                val angle = (i * PI / points).toFloat()
                val r = if (i % 2 == 0) outerRadius else {
                    lerp(outerRadius, innerRadius, shapePercent)
                }
                lineTo(centerX + r * cos(angle), centerY + r * sin(angle))
            }
            close()
        }
    }

    Button(
        onClick = { if (isConnected) onDisconnect() else onConnect() },
        modifier = Modifier
            .size(120.dp, 56.dp)
            .clip(burstShape),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isError) MaterialTheme.colorScheme.error
            else if (isConnected) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.primary,
            contentColor = if (isConnected && !isError) MaterialTheme.colorScheme.onSecondaryContainer
            else MaterialTheme.colorScheme.onPrimary
        ),
        shape = if (isError) burstShape else RoundedCornerShape(16.dp)
    ) {
        Text(
            text = if (isError) "RETRY" else if (isConnected) "STOP" else "START",
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun VisualizerSection(state: DashboardState, onResetOrigin: () -> Unit, onToggleRotationMode: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Rotating Cube using Quaternion
            RotatingCube(
                quat = state.quaternion,
                displacement = state.gyro,
                modifier = Modifier.fillMaxSize()
            )

            // Origin Reset & Mode Toggle
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = onToggleRotationMode,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        state.rotationMode,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onResetOrigin,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset Origin", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }

            // Overlaid Data
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                DataRow(label = "RATE", value = "%.1f pkt/s".format(state.packetRate))
                DataRow(label = "RSSI", value = "${state.rssi} dBm")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DataRow(label = "X", value = state.rotation.x.toInt().toString())
                    DataRow(label = "Y", value = state.rotation.y.toInt().toString())
                    DataRow(label = "Z", value = state.rotation.z.toInt().toString())
                }
                DataRow(label = "G-VEC", value = "[%.1f, %.1f, %.1f]".format(state.gyro.x, state.gyro.y, state.gyro.z))
            }
        }
    }
}

@Composable
fun DataRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RotatingCube(quat: Quaternion, displacement: Vec3, modifier: Modifier) {
    Canvas(modifier = modifier) {
        val canvasSize = size
        val cubeScale = min(canvasSize.width, canvasSize.height) * 0.15f

        val vertices = listOf(
            floatArrayOf(-1f, -1f, -1f), floatArrayOf(1f, -1f, -1f),
            floatArrayOf(1f, 1f, -1f), floatArrayOf(-1f, 1f, -1f),
            floatArrayOf(-1f, -1f, 1f), floatArrayOf(1f, -1f, 1f),
            floatArrayOf(1f, 1f, 1f), floatArrayOf(-1f, 1f, 1f)
        )

        // Project vertices using Quaternion
        val projected = vertices.map { v ->
            val p = Quaternion(v[0], v[1], v[2], 0f)
            val qInv = Quaternion(-quat.x, -quat.y, -quat.z, quat.w)
            
            // v' = q * p * q_inv
            fun qMul(a: Quaternion, b: Quaternion) = Quaternion(
                a.w * b.x + a.x * b.w + a.y * b.z - a.z * b.y,
                a.w * b.y + a.y * b.w + a.z * b.x - a.x * b.z,
                a.w * b.z + a.z * b.w + a.x * b.y - a.y * b.x,
                a.w * b.w - a.x * b.x - a.y * b.y - a.z * b.z
            )
            
            val pRot = qMul(qMul(quat, p), qInv)

            // Calculate translation based on non-gravity acceleration (jiggle)
            // We expect gravity to be roughly (0, 0, 1) or similar in magnitude 1.0
            // The RetargetingSmoother is sending G-Vec + Jiggle in the gyro field.
            // For visualization, we'll amplify the jiggle.
            val dx = displacement.x * 20f
            val dy = displacement.y * 20f

            Offset(
                (pRot.x * cubeScale + center.x + dx),
                (pRot.y * cubeScale + center.y + dy)
            )
        }

        val edges = listOf(
            0 to 1, 1 to 2, 2 to 3, 3 to 0,
            4 to 5, 5 to 6, 6 to 7, 7 to 4,
            0 to 4, 1 to 5, 2 to 6, 3 to 7
        )

        edges.forEach { (start, end) ->
            drawLine(
                color = Color.Cyan.copy(alpha = 0.7f),
                start = projected[start],
                end = projected[end],
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        
        // Draw vertices for "nodes" look
        projected.forEach { pos ->
            drawCircle(Color.White, radius = 4.dp.toPx(), center = pos)
        }
    }
}

@Composable
fun ControlSection(
    state: DashboardState,
    onToggleSound: () -> Unit,
    onUpdateSmoothing: (Float) -> Unit,
    onUpdateGain: (Float) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Live Controls",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ControlCard(
                title = "Audio Engine",
                icon = if (state.isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                modifier = Modifier.weight(1f),
                onClick = onToggleSound,
                containerColor = if (state.isSoundEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(if (state.isSoundEnabled) "RUNNING" else "MUTED", style = MaterialTheme.typography.labelLarge)
            }

            ControlCard(
                title = "Smoothing",
                icon = Icons.Default.Tune, // Fallback for SlowMotionVideo
                modifier = Modifier.weight(1f)
            ) {
                Slider(
                    value = state.smoothing,
                    onValueChange = onUpdateSmoothing,
                    valueRange = 0.15f..1.5f,
                    modifier = Modifier.height(24.dp)
                )
                Text("%.2fs".format(state.smoothing), style = MaterialTheme.typography.labelSmall)
            }
        }

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Master Gain", style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = state.masterGain,
                    onValueChange = onUpdateGain,
                    valueRange = 0f..2f
                )
            }
        }
    }
}

@Composable
fun ControlCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            content()
        }
    }
}

@Composable
fun MappingSection(
    state: DashboardState,
    onToggleAxis: (Int, Boolean) -> Unit,
    onUpdateRange: (Int, ClosedFloatingPointRange<Float>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            Row(
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Axis Mapping", style = MaterialTheme.typography.titleSmall)
                }
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }

            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    AxisControl(
                        label = "X-Axis (Pitch)",
                        enabled = state.axisEnabled.first,
                        range = state.xRange,
                        onToggle = { onToggleAxis(0, it) },
                        onRangeChange = { onUpdateRange(0, it) }
                    )
                    AxisControl(
                        label = "Y-Axis (Roll)",
                        enabled = state.axisEnabled.second,
                        range = state.yRange,
                        onToggle = { onToggleAxis(1, it) },
                        onRangeChange = { onUpdateRange(1, it) }
                    )
                    AxisControl(
                        label = "Z-Axis (Yaw)",
                        enabled = state.axisEnabled.third,
                        range = state.zRange,
                        onToggle = { onToggleAxis(2, it) },
                        onRangeChange = { onUpdateRange(2, it) }
                    )
                }
            }
        }
    }
}

@Composable
fun AxisControl(
    label: String,
    enabled: Boolean,
    range: ClosedFloatingPointRange<Float>,
    onToggle: (Boolean) -> Unit,
    onRangeChange: (ClosedFloatingPointRange<Float>) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
        RangeSlider(
            value = range.start..range.endInclusive,
            onValueChange = { onRangeChange(it.start..it.endInclusive) },
            valueRange = 50f..1500f,
            enabled = enabled
        )
        Text(
            "Range: ${range.start.toInt()} - ${range.endInclusive.toInt()} Hz",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun DiagnosticsSection(state: DashboardState) {
    var expanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            Row(
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("System Diagnostics", style = MaterialTheme.typography.titleSmall)
                }
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }

            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.Black.copy(alpha = 0.05f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = state.logs.takeLast(20).joinToString("\n"),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }
            }
        }
    }
}

private fun lerp(start: Float, end: Float, fraction: Float): Float {
    return start + fraction * (end - start)
}

@Preview(showBackground = true)
@Composable
fun HeaderPreview() {
    RingDemoTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            HeaderSection(
                state = DashboardState(status = ConnectionStatus.ConnectedIdle),
                onConnect = {},
                onDisconnect = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VisualizerPreview() {
    RingDemoTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            VisualizerSection(
                state = DashboardState(
                    rotation = Vec3(30f, 60f, 10f),
                    packetRate = 45.5,
                    rssi = -72
                ),
                onResetOrigin = {},
                onToggleRotationMode = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ControlSectionPreview() {
    RingDemoTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ControlSection(
                state = DashboardState(isSoundEnabled = true),
                onToggleSound = {},
                onUpdateSmoothing = {},
                onUpdateGain = {}
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DashboardPreview() {
    val state = DashboardState(
        status = ConnectionStatus.Streaming,
        rotation = Vec3(45f, 45f, 45f),
        rssi = -65,
        packetRate = 50.0,
        isSoundEnabled = true,
        logs = listOf("Connected to Ring", "Battery: 85%", "Streaming data...")
    )
    RingDemoTheme {
        DashboardScreen(
            state = state,
            onConnect = {},
            onDisconnect = {},
            onResetOrigin = {},
            onToggleRotationMode = {},
            onToggleSound = {},
            onUpdateSmoothing = {},
            onUpdateGain = {},
            onToggleAxis = { _, _ -> },
            onUpdateRange = { _, _ -> },
            onClearLogs = {}
        )
    }
}
