package com.mrm.amarisca.viewmodel

import MqttManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mrm.amarisca.data.local.entities.*
import com.mrm.amarisca.data.repository.SmartHomeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SmartHomeUiState(
    val gateways: List<GatewayWithSwitches> = emptyList(),
    val lastAckPerGateway: Map<String, String> = emptyMap(),
    val showAddGatewayDialog: Boolean = false,
    val showEditGatewayDialogFor: SmartGateway? = null,
    val showEditSwitchDialogFor: SmartSwitch? = null
)

class SmartHomeViewModel(
    private val repository: SmartHomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmartHomeUiState())
    val uiState: StateFlow<SmartHomeUiState> = _uiState.asStateFlow()

    // gatewayIdString -> mqttManager
    private val mqttByGatewayId = mutableMapOf<String, MqttManager>()

    init {
        viewModelScope.launch {
            repository.getGatewaysWithSwitches().collect { list ->
                _uiState.update { it.copy(gateways = list) }

                if (list.isEmpty()) {
                    _uiState.update { it.copy(showAddGatewayDialog = true) }
                }
            }
        }
    }

    fun onAddGatewayClicked() {
        _uiState.update { it.copy(showAddGatewayDialog = true) }
    }

    fun dismissAddGatewayDialog() {
        _uiState.update { it.copy(showAddGatewayDialog = false) }
    }

    fun addGateway(gatewayId: String, name: String) {
        viewModelScope.launch {
            repository.addGatewayWithDefaultSwitches(gatewayId, name)
            _uiState.update { it.copy(showAddGatewayDialog = false) }
        }
    }

    fun showEditGatewayDialog(gateway: SmartGateway) {
        _uiState.update { it.copy(showEditGatewayDialogFor = gateway) }
    }

    fun dismissEditGatewayDialog() {
        _uiState.update { it.copy(showEditGatewayDialogFor = null) }
    }
    fun updateGateway(gateway: SmartGateway, newName: String, newGatewayId: String) {
        viewModelScope.launch {

            // If gatewayId changes → remove old MQTT manager
            if (gateway.gatewayId != newGatewayId) {
                mqttByGatewayId[gateway.gatewayId]?.let {
                    mqttByGatewayId.remove(gateway.gatewayId)
                }
            }

            repository.updateGateway(gateway, newName, newGatewayId)

            _uiState.update { it.copy(showEditGatewayDialogFor = null) }
        }
    }

    fun deleteGateway(gateway: SmartGateway) {
        viewModelScope.launch {

            // remove MQTT link
            mqttByGatewayId.remove(gateway.gatewayId)

            // delete DB
            repository.deleteGateway(gateway)

            // close popup
            _uiState.update { it.copy(showEditGatewayDialogFor = null) }
        }
    }


    fun showEditSwitchDialog(switch: SmartSwitch) {
        _uiState.update { it.copy(showEditSwitchDialogFor = switch) }
    }

    fun dismissEditSwitchDialog() {
        _uiState.update { it.copy(showEditSwitchDialogFor = null) }
    }

    fun updateSwitch(switch: SmartSwitch, newName: String, newType: String) {
        viewModelScope.launch {
            repository.updateSwitch(switch, newName, newType)
            _uiState.update { it.copy(showEditSwitchDialogFor = null) }
        }
    }


    // ----------------------------------------------------
    // MQTT Handling
    // ----------------------------------------------------

    private fun ensureMqttForGateway(gateway: SmartGateway) {
        if (mqttByGatewayId.containsKey(gateway.gatewayId)) return

        val manager = MqttManager(
            gateway.id,          // DB ID (Long)
            gateway.gatewayId    // actual gatewayId string
        ) { ack ->
            _uiState.update { old ->
                val newMap = old.lastAckPerGateway.toMutableMap()
                newMap[gateway.gatewayId] = ack
                old.copy(lastAckPerGateway = newMap)
            }
        }

        mqttByGatewayId[gateway.gatewayId] = manager
        manager.connect()
    }


    fun toggleSwitch(gateway: SmartGateway, switch: SmartSwitch, on: Boolean) {
        ensureMqttForGateway(gateway)

        val manager = mqttByGatewayId[gateway.gatewayId] ?: return

        if (!manager.isConnected()) {
            println("MQTT not connected yet for gateway ${gateway.gatewayId}")
            return
        }

        manager.publishSwitchCommand(switch.switchIndex, on)
    }
}