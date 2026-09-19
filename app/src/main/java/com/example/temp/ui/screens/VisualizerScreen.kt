package com.example.temp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.temp.data.VisualizerType
import com.example.temp.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualizerScreen(nav: NavHostController, vm: MainViewModel) {
    val s by vm.settings.collectAsStateWithLifecycle()
    val player by vm.playerController.state.collectAsStateWithLifecycle()

    // Fallback: синтетические данные, если визуализатор аудио недоступен.
    // Объяснение: Visualizer требует android.permission.RECORD_AUDIO на части устройств,
    // а связка с ExoPlayer session id не всегда возвращает данные.
    // Поэтому в первой версии используется плавная fallback-анимация,
    // реагирующая на состояние плеера (играет/пауза) и жанр (тип визуализации).
    val bars = remember { mutableStateListOf<Float>() }
    LaunchedEffect(s.visualizerBars) {
        bars.clear()
        repeat(s.visualizerBars) { bars.add(0.05f) }
    }
    LaunchedEffect(s.visualizerEnabled, player.isPlaying, s.visualizerType) {
        while (true) {
            val active = s.visualizerEnabled && player.isPlaying
            for (i in bars.indices) {
                val target = if (active)
                    (Math.random() * s.visualizerSensitivity * 0.9f + 0.05f).toFloat()
                else 0.05f
                bars[i] = bars[i] + (target - bars[i]) * 0.25f
            }
            kotlinx.coroutines.delay(50)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Визуализация") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { p ->
        Column(Modifier.fillMaxSize().padding(p)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    when (s.visualizerType) {
                        VisualizerType.SPECTRUM -> {
                            val n = bars.size
                            val bw = w / n
                            bars.forEachIndexed { i, v ->
                                val barH = (v.coerceIn(0f, 1f)) * h * 0.9f
                                drawRect(
                                    color = Color(0xFF14B8A6).copy(alpha = 0.9f),
                                    topLeft = Offset(i * bw + bw * 0.15f, h - barH),
                                    size = androidx.compose.ui.geometry.Size(bw * 0.7f, barH)
                                )
                            }
                        }
                        VisualizerType.WAVE -> {
                            val path = Path()
                            path.moveTo(0f, h / 2)
                            bars.forEachIndexed { i, v ->
                                val x = w * i / (bars.size - 1).coerceAtLeast(1)
                                val y = h / 2 + (v - 0.5f) * h * 0.6f
                                path.lineTo(x, y)
                            }
                            drawPath(path, Color(0xFF38BDF8), style = Stroke(width = 6f))
                        }
                        VisualizerType.CIRCLE -> {
                            val cx = w / 2; val cy = h / 2
                            val baseR = minOf(w, h) * 0.25f
                            bars.forEachIndexed { i, v ->
                                val angle = (i.toFloat() / bars.size) * 2 * Math.PI
                                val r = baseR + v * baseR * 0.8f
                                val x = cx + (r * kotlin.math.cos(angle)).toFloat()
                                val y = cy + (r * kotlin.math.sin(angle)).toFloat()
                                drawCircle(Color(0xFF8B5CF6), radius = 6f, center = Offset(x, y))
                            }
                        }
                    }
                }
            }

            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text("Включено")
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = s.visualizerEnabled,
                        onCheckedChange = vm::setVisualizerEnabled
                    )
                }
                Row {
                    VisualizerType.values().forEach { t ->
                        FilterChip(
                            selected = s.visualizerType == t,
                            onClick = { vm.setVisualizerType(t) },
                            label = { Text(t.name) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
                Text("Полос: ${s.visualizerBars}")
                Slider(
                    value = s.visualizerBars.toFloat(),
                    onValueChange = { vm.setVisualizerBars(it.toInt()) },
                    valueRange = 8f..64f
                )
                Text("Чувствительность: %.2f".format(s.visualizerSensitivity))
                Slider(
                    value = s.visualizerSensitivity,
                    onValueChange = { vm.setVisualizerSensitivity(it) },
                    valueRange = 0.3f..2.5f
                )
            }
        }
    }
}
