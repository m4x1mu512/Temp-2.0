package com.example.temp.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.temp.ui.MainViewModel

@Composable
fun SleepTimerDialog(vm: MainViewModel, onDismiss: () -> Unit) {
    val s by vm.settings.collectAsStateWithLifecycle()
    val opts = listOf(0 to "Выкл", 15 to "15 минут", 30 to "30 минут", 45 to "45 минут",
        60 to "60 минут", -1 to "Конец текущего трека")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Таймер сна") },
        text = {
            Column {
                opts.forEach { (v, label) ->
                    Row(
                        modifier = Modifier.selectable(
                            selected = s.sleepTimerMinutes == v,
                            onClick = { vm.setSleepTimer(v); onDismiss() }
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = s.sleepTimerMinutes == v, onClick = null)
                        Text(label)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("ОК") } }
    )
}
