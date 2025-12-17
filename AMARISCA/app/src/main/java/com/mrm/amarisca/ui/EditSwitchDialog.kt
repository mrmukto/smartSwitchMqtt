package com.mrm.amarisca.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.mrm.amarisca.data.local.entities.SmartSwitch

@Composable
fun EditSwitchDialog(
    switch: SmartSwitch,
    onConfirm: (newName: String, newType: String) -> Unit,
    onDismiss: () -> Unit
) {
    var nameField by remember {
        mutableStateOf(TextFieldValue(switch.switchName))
    }

    var typeField by remember {
        mutableStateOf(TextFieldValue(switch.switchType))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Switch") },
        text = {
            Column {
                OutlinedTextField(
                    value = nameField,
                    onValueChange = { nameField = it },
                    label = { Text("Switch Name") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = typeField,
                    onValueChange = { typeField = it },
                    label = { Text("Switch Type (Light/Fan/Other)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(nameField.text, typeField.text)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
