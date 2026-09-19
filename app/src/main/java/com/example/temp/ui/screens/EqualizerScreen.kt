package com.example.temp.ui.screens

import android.media.audiofx.Equalizer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(nav: NavHostController) {
    val ctx = LocalContext.current
    var eq by remember { mutableStateOf<Equalizer?>(null) }
    var bands by remember { mutableStateOf(emptyList<Pair<Short, Int>>()) } // centerFreq mHz -> level
    var preset by remember { mutableIntStateOf(-1) }

    DisposableEffect(Unit) {
        val e = runCatching { Equalizer(0, 0) }.getOrNull()
        if (e != null) {
            e.enabled = true
            bands = (0 until e.numberOfBands).map { i ->
                val b = e.getBand(i)
                val range = e.bandLevelRange
                val level = if (i < 5) (range[0] + (range[1] - range[0]) / 2) else 0
                e.setBandLevel(b, level.toShort())
                (b.centerFreq.toInt() to level)
            }
        }
        eq = e
        onDispose { e?.release() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Эквалайзер") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { p ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(p)
                .padding(16.dp)
        ) {
            if (eq == null) {
                Text("Эквалайзер недоступен на этом устройстве")
            } else {
                val e = eq!!
                Text("Пресеты", style = MaterialTheme.typography.titleMedium)
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    val presets = (0 until e.numberOfPresets).map { e.getPresetName(it) }
                    presets.forEachIndexed { i, name ->
                        FilterChip(
                            selected = preset == i,
                            onClick = {
                                runCatching { e.usePreset(i.toShort()) }
                                bands = (0 until e.numberOfBands).map { b ->
                                    val band = e.getBand(b)
                                    (band.centerFreq.toInt() to e.getBandLevel(b).toInt())
                                }
                                preset = i
                            },
                            label = { Text(name) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Полосы", style = MaterialTheme.typography.titleMedium)
                bands.forEachIndexed { idx, (freq, level) ->
                    Column(Modifier.padding(vertical = 4.dp)) {
                        Text("${freq / 1000} Гц — $level")
                        Slider(
                            value = level.toFloat(),
                            onValueChange = { v ->
                                runCatching {
                                    e.setBandLevel(idx.toShort(), v.toInt().toShort())
                                    bands = bands.toMutableList().also {
                                        it[idx] = freq to v.toInt()
                                    }
                                }
                            },
                            valueRange = e.bandLevelRange[0].toFloat()..e.bandLevelRange[1].toFloat(),
                            steps = (e.bandLevelRange[1] - e.bandLevelRange[0] - 1).toInt()
                        )
                    }
                }
            }
        }
    }
}
