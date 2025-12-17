package com.mrm.amarisca.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mrm.amarisca.viewmodel.SmartHomeVMFactory
import com.mrm.amarisca.viewmodel.SmartHomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartHomeScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vm: SmartHomeViewModel = viewModel(factory = SmartHomeVMFactory(context))
    val state by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Switch Gateways") },
                actions = {
                    IconButton(onClick = { vm.onAddGatewayClicked() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Gateway")
                    }
                }
            )
        }
    ) { innerPadding ->

        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            if (state.gateways.isEmpty()) {
                Text(
                    "No gateways yet. Tap + to add.",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    items(state.gateways, key = { it.gateway!!.id }) { gw ->
                        GatewayCard(
                            gatewayWithSwitches = gw,
                            lastAck = state.lastAckPerGateway[gw.gateway?.gatewayId],
                            onSwitchToggle = { sw, on -> vm.toggleSwitch(gw.gateway!!, sw, on) },
                            onEditGatewayClick = { vm.showEditGatewayDialog(gw.gateway!!) },
                            onEditSwitchClick = { sw -> vm.showEditSwitchDialog(sw) }
                        )
                    }
                }
            }

            if (state.showAddGatewayDialog) {
                AddGatewayDialog(
                    onConfirm = { id, name -> vm.addGateway(id, name) },
                    onDismiss = { vm.dismissAddGatewayDialog() }
                )
            }

            state.showEditGatewayDialogFor?.let { gateway ->
                EditGatewayDialog(
                    gateway = gateway,
                    onConfirm = { newName, newGatewayId ->
                        vm.updateGateway(gateway, newName, newGatewayId)
                    },
                    onDelete = {
                        vm.deleteGateway(gateway)
                    },
                    onDismiss = { vm.dismissEditGatewayDialog() }
                )
            }


            state.showEditSwitchDialogFor?.let { sw ->
                EditSwitchDialog(
                    switch = sw,
                    onConfirm = { newName, newType -> vm.updateSwitch(sw, newName, newType) },
                    onDismiss = { vm.dismissEditSwitchDialog() }
                )
            }
        }
    }
}
