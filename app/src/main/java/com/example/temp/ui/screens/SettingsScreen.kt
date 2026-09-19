package com.example.temp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.temp.BuildConfig
import com.example.temp.data.ThemeMode
import com.example.temp.data.VisualizerType
import com.example.temp.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(nav: NavHostController, vm: MainViewModel) {
    val s by vm.settings.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
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
            Text("Тема", style = MaterialTheme.typography.titleMedium)
            Row {
                ThemeMode.values().forEach { t ->
                    FilterChip(
                        selected = s.theme == t,
                        onClick = { vm.setTheme(t) },
                        label = {
                            Text(
                                when (t) {
                                    ThemeMode.LIGHT -> "Светлая"
                                    ThemeMode.DARK -> "Тёмная"
                                    ThemeMode.SYSTEM -> "Системная"
                                }
                            )
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Визуализация", style = MaterialTheme.typography.titleMedium)
            SwitchRow(
                title = "Включить",
                checked = s.visualizerEnabled,
                onCheckedChange = vm::setVisualizerEnabled
            )
            Row {
                VisualizerType.values().forEach { t ->
                    FilterChip(
                        selected = s.visualizerType == t,
                        onClick = { vm.setVisualizerType(t) },
                        label = {
                            Text(
                                when (t) {
                                    VisualizerType.SPECTRUM -> "Спектр"
                                    VisualizerType.WAVE -> "Волна"
                                    VisualizerType.CIRCLE -> "Круг"
                                }
                            )
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
            Text("Полос: ${s.visualizerBars}")
            Slider(
                value = s.visualizerBars.toFloat(),
                onValueChange = { vm.setVisualizerBars(it.toInt()) },
                valueRange = 8f..64f,
                steps = 55
            )
            Text("Чувствительность: %.2f".format(s.visualizerSensitivity))
            Slider(
                value = s.visualizerSensitivity,
                onValueChange = { vm.setVisualizerSensitivity(it) },
                valueRange = 0.3f..2.5f
            )

            Spacer(Modifier.height(16.dp))
            Text("Таймер сна", style = MaterialTheme.typography.titleMedium)
            SleepTimerSettings(vm)

            Spacer(Modifier.height(16.dp))
            Button(onClick = { vm.scan() }, modifier = Modifier.fillMaxWidth()) {
                Text("Пересмотреть медиатеку")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { vm.resetSettings() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Сбросить настройки") }

            Spacer(Modifier.height(24.dp))
            Text("О приложении", style = MaterialTheme.typography.titleMedium)
            Text("Темп v${BuildConfig.VERSION_NAME}")
            Text(
                "Приложение воспроизводит только локальные аудиофайлы на устройстве. " +
                "Оно не отправляет данные в интернет и не собирает персональную информацию.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SwitchRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SleepTimerSettings(vm: MainViewModel) {
    val options = listOf(0 to "Выкл", 15 to "15 мин", 30 to "30 мин", 45 to "45 мин",
        60 to "60 мин", -1 to "Конец трека")
    val current by vm.settings.collectAsStateWithLifecycle()
    Column {
        options.forEach { (min, label) ->
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                RadioButton(
                    selected = current.sleepTimerMinutes == min,
                    onClick = { vm.setSleepTimer(min) }
                )
                Text(label)
            }
        }
    }
}
