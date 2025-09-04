package com.gw.safedelivery.ui.dialog

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun StoreUrlInputDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(TextFieldValue("")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("URL 입력") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("업소 URL") }
            )
        },
        confirmButton = {
            TextButton(onClick = {
                if (text.text.isNotBlank()) onConfirm(text.text.trim())
            }) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        }
    )
}
