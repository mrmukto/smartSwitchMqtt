package com.mrm.amarisca.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.mrm.amarisca.data.local.entities.SmartGateway

@Composable
fun EditGatewayDialog(
    gateway: SmartGateway,
    onConfirm: (newName: String, newGatewayId: String) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var nameField by remember { mutableStateOf(gateway.name) }
    var idField by remember { mutableStateOf(gateway.gatewayId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Gateway") },

        text = {
            Column {
                OutlinedTextField(
                    value = nameField,
                    onValueChange = { nameField = it },
                    label = { Text("Gateway Name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = idField,
                    onValueChange = { idField = it },
                    label = { Text("Gateway Number") }
                )
            }
        },

        confirmButton = {
            TextButton(onClick = {
                if (idField.isNotBlank())
                    onConfirm(nameField, idField)
            }) {
                Text("Update")
            }
        },

        dismissButton = {
            Row {
                TextButton(onClick = onDelete) {
                    Text("Delete", color = Color.Red)
                }
                Spacer(modifier = Modifier.width(12.dp))
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
