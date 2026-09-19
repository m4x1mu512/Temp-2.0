package com.example.temp.ui.screens

import android.media.audiofx.Equalizer
import androidx.compose.foundation.horizontalScroll
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
    var bands by remember { mutableStateOf(emptyList<Triple<Short, Int, Short>>()) }
    // Triple(bandIndex, centerFreqHz, level)
    var preset by remember { mutableIntStateOf(-1) }

    DisposableEffect(Unit) {
        val e = runCatching { Equalizer(0, 0) }.getOrNull()
        if (e != null) {
            e.enabled = true
            val range = e.bandLevelRange
            val mid = ((range[0].toInt() + range[1].toInt()) / 2).toShort()
            bands = (0 until e.numberOfBands).map { i ->
                val b = i.toShort()
                e.setBandLevel(b, mid)
                Triple(b, e.getCenterFreq(b), mid)
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
            val e = eq
            if (e == null) {
                Text("Эквалайзер недоступен на этом устройстве")
            } else {
                Text("Пресеты", style = MaterialTheme.typography.titleMedium)
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    val count = e.numberOfPresets.toInt()
                    (0 until count).forEach { i ->
                        val name = runCatching { e.getPresetName(i.toShort()) }
                            .getOrDefault("Preset $i")
                        FilterChip(
                            selected = preset == i,
                            onClick = {
                                runCatching { e.usePreset(i.toShort()) }
                                bands = (0 until e.numberOfBands).map { b ->
                                    val bb = b.toShort()
                                    Triple(bb, e.getCenterFreq(bb), e.getBandLevel(bb))
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

                val range = e.bandLevelRange
                bands.forEach { (bandIdx, freqHz, level) ->
                    Column(Modifier.padding(vertical = 4.dp)) {
                        Text("${freqHz / 1000} Гц — $level")
                        Slider(
                            value = level.toFloat(),
                            onValueChange = { v ->
                                val newLevel = v.toInt().toShort()
                                runCatching {
                                    e.setBandLevel(bandIdx, newLevel)
                                    bands = bands.map {
                                        if (it.first == bandIdx) Triple(bandIdx, freqHz, newLevel)
                                        else it
                                    }
                                }
                            },
                            valueRange = range[0].toFloat()..range[1].toFloat(),
                            steps = (range[1] - range[0] - 1).toInt().coerceAtLeast(0)
                        )
                    }
                }
            }
        }
    }
}