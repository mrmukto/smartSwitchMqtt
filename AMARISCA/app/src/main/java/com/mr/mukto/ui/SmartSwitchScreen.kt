package com.mr.mukto.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mr.mukto.viewmodel.SmartHomeVMFactory
import com.mr.mukto.viewmodel.SmartHomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartHomeScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vm: SmartHomeViewModel = viewModel(factory = SmartHomeVMFactory(context))
    val state by vm.uiState.collectAsState()

    val background = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background.copy(alpha = 0.92f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "ইচ্ছা",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            "Smart Switch Control",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                },
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
                .background(background)
                .padding(innerPadding)
        ) {

            if (state.gateways.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    tonalElevation = 2.dp,
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No gateways yet",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Tap + to add your first device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    items(state.gateways, key = { it.gateway!!.id }) { gw ->
                        GatewayCard(
                            gatewayWithSwitches = gw,
                            lastAck = state.lastAckPerGateway[gw.gateway?.gatewayId],
                            switchStates = state.switchStates[gw.gateway?.gatewayId].orEmpty(),
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
