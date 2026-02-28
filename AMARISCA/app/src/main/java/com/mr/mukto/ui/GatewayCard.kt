package com.mr.mukto.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mr.mukto.data.local.entities.GatewayWithSwitches
import com.mr.mukto.data.local.entities.SmartSwitch

@Composable
fun GatewayCard(
    gatewayWithSwitches: GatewayWithSwitches,
    lastAck: String?,
    switchStates: Map<Int, Boolean>,
    onSwitchToggle: (SmartSwitch, Boolean) -> Unit,
    onEditGatewayClick: () -> Unit,
    onEditSwitchClick: (SmartSwitch) -> Unit
) {
    val gateway = gatewayWithSwitches.gateway!!
    val switches = gatewayWithSwitches.switches

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), MaterialTheme.shapes.large)
                    .padding(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        gateway.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "ID: ${gateway.gatewayId}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                TextButton(onClick = onEditGatewayClick) {
                    Text("Edit")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            lastAck?.let {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                ) {
                    Text(
                        "ACK: $it",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            switches.forEach { sw ->
                SwitchRow(
                    smartSwitch = sw,
                    isOn = switchStates[sw.switchIndex] ?: false,
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
    isOn: Boolean,
    onToggle: (SmartSwitch, Boolean) -> Unit,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(MaterialTheme.shapes.medium)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), MaterialTheme.shapes.medium)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                smartSwitch.switchName,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                "Type: ${smartSwitch.switchType}",
                style = MaterialTheme.typography.bodySmall
            )

            val stateText = if (isOn) "ON" else "OFF"
            val stateColor = if (isOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            Text(
                stateText,
                style = MaterialTheme.typography.labelLarge,
                color = stateColor
            )
        }

        Row {
            TextButton(onClick = onEditClick) {
                Text("Edit")
            }

            Switch(
                checked = isOn,
                onCheckedChange = {
                    onToggle(smartSwitch, it)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                    uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            )
        }
    }
}
