package top.focess.netdesign.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import top.focess.netdesign.config.LangFile

@Composable
fun LangFile.LangScope.SettingsView(_host: String, _port: Int, saveSettings: (host: String, port: Int) -> Unit) {
    var host by remember { mutableStateOf(_host) }
    var port by remember { mutableStateOf(_port) }
    var save by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
        TextField(
            host,
            onValueChange = { host = it },
            modifier = Modifier.fillMaxWidth(0.8f).weight(2f),
            label = { Text("settings.host".l) },
            singleLine = true
        )

        Spacer(Modifier.width(8.dp))

        IntTextField(
            port.toString(),
            port,
            onValueChange = { port = it },
            modifier = Modifier.fillMaxWidth(0.8f).weight(1f),
            label = { Text("settings.port".l) },
            singleLine = true,
            left = 0,
            right = 65535
        )
    }

    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
        Button(onClick = {
            save = true
        }) {
            Text("settings.save".l)
        }
    }

    LaunchedEffect(save) {
        if (save) {
            delay(200)
            saveSettings(host, port)
        }
    }

}