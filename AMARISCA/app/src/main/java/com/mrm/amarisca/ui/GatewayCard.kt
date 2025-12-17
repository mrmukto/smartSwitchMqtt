package com.mrm.amarisca.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mrm.amarisca.data.local.entities.GatewayWithSwitches
import com.mrm.amarisca.data.local.entities.SmartSwitch

@Composable
fun GatewayCard(
    gatewayWithSwitches: GatewayWithSwitches,
    lastAck: String?,
    onSwitchToggle: (SmartSwitch, Boolean) -> Unit,
    onEditGatewayClick: () -> Unit,
    onEditSwitchClick: (SmartSwitch) -> Unit
) {
    val gateway = gatewayWithSwitches.gateway!!
    val switches = gatewayWithSwitches.switches

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        gateway.name,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        "ID: ${gateway.gatewayId}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                TextButton(onClick = onEditGatewayClick) {
                    Text("Edit")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            lastAck?.let {
                Text("Last ACK: $it", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
            }

            switches.forEach { sw ->
                SwitchRow(
                    smartSwitch = sw,
                    onToggle = onSwitchToggle,
                    onEditClick = { onEditSwitchClick(sw) }
                )
            }
        }
    }
}

@Composable
private fun SwitchRow(
    smartSwitch: SmartSwitch,
    onToggle: (SmartSwitch, Boolean) -> Unit,
    onEditClick: () -> Unit
) {
    var isOn by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                smartSwitch.switchName,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                "Type: ${smartSwitch.switchType}",
                style = MaterialTheme.typography.bodySmall
            )
        }

        Row {
            TextButton(onClick = onEditClick) {
                Text("Edit")
            }

            Switch(
                checked = isOn,
                onCheckedChange = {
                    isOn = it
                    onToggle(smartSwitch, it)
                }
            )
        }
    }
}
