package com.gw.safedelivery.ui.dialog

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun ViolationDialog(result: String, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onClose() },
        title = { Text("행정처분 확인") },
        text = { Text(result) },
        confirmButton = {
            TextButton(onClick = { onClose() }) { Text("확인") }
        }
    )
}
