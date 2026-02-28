package com.mr.mukto.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun AddGatewayDialog(
    onConfirm: (gatewayId: String, name: String) -> Unit,
    onDismiss: () -> Unit
) {
    var idField by remember { mutableStateOf(TextFieldValue("")) }
    var nameField by remember { mutableStateOf(TextFieldValue("")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Smart Gateway") },
        text = {
            Column {
                OutlinedTextField(
                    value = idField,
                    onValueChange = { idField = it },
                    label = { Text("Gateway Number") }
                )
                OutlinedTextField(
                    value = nameField,
                    onValueChange = { nameField = it },
                    label = { Text("Gateway Name") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (idField.text.isNotBlank()) {
                    onConfirm(idField.text.trim(), nameField.text.trim())
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
