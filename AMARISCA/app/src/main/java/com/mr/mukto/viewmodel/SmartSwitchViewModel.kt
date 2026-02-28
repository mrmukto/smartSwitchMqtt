package com.mr.mukto.viewmodel

import MqttManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mr.mukto.data.local.entities.*
import com.mr.mukto.data.repository.SmartHomeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SmartHomeUiState(
    val gateways: List<GatewayWithSwitches> = emptyList(),
    val lastAckPerGateway: Map<String, String> = emptyMap(),
    val switchStates: Map<String, Map<Int, Boolean>> = emptyMap(),
    val showAddGatewayDialog: Boolean = false,
    val showEditGatewayDialogFor: SmartGateway? = null,
    val showEditSwitchDialogFor: SmartSwitch? = null
)

class SmartHomeViewModel(
    private val repository: SmartHomeRepository
) : ViewModel() {

    private val tag = "SmartHomeVM"

    private val _uiState = MutableStateFlow(SmartHomeUiState())
    val uiState: StateFlow<SmartHomeUiState> = _uiState.asStateFlow()

    // gatewayIdString -> mqttManager
    private val mqttByGatewayId = mutableMapOf<String, MqttManager>()

    init {
        viewModelScope.launch {
            repository.getGatewaysWithSwitches().collect { list ->
                val stateMap = buildMap {
                    list.forEach { gw ->
                        val gid = gw.gateway?.gatewayId ?: return@forEach
                        val swStates = mutableMapOf<Int, Boolean>()
                        gw.switches.forEach { sw ->
                            swStates[sw.switchIndex] = sw.isOn
                        }
                        put(gid, swStates)
                    }
                }

                val ackMap = buildMap {
                    list.forEach { gw ->
                        val gid = gw.gateway?.gatewayId ?: return@forEach
                        gw.gateway?.lastAck?.let { put(gid, it) }
                    }
                }

                _uiState.update {
                    it.copy(
                        gateways = list,
                        switchStates = stateMap,
                        lastAckPerGateway = ackMap
                    )
                }

                // Proactively connect MQTT for all gateways so toggles work immediately.
                list.mapNotNull { it.gateway }.forEach { gateway ->
                    Log.d(tag, "Ensuring MQTT for gateway=${gateway.gatewayId}")
                    ensureMqttForGateway(gateway)
                }

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

        Log.d(tag, "Creating MQTT manager for gateway=${gateway.gatewayId}")
        val manager = MqttManager(
            gateway.id,          // DB ID (Long)
            gateway.gatewayId    // actual gatewayId string
        ) { ack ->
            Log.d(tag, "ACK Update → gw=${ack.gatewayId} sw=${ack.switchIndex} on=${ack.isOn}")
            _uiState.update { old ->
                val newMap = old.lastAckPerGateway.toMutableMap()
                newMap[gateway.gatewayId] = ack.raw

                val gatewayStates = old.switchStates[gateway.gatewayId]?.toMutableMap()
                    ?: mutableMapOf()
                gatewayStates[ack.switchIndex] = ack.isOn

                val newStates = old.switchStates.toMutableMap()
                newStates[gateway.gatewayId] = gatewayStates

                old.copy(
                    lastAckPerGateway = newMap,
                    switchStates = newStates
                )
            }

            // Persist last known state to DB for app restarts.
            viewModelScope.launch {
                val swId = _uiState.value.gateways
                    .firstOrNull { it.gateway?.gatewayId == ack.gatewayId }
                    ?.switches
                    ?.firstOrNull { it.switchIndex == ack.switchIndex }
                    ?.id
                if (swId != null) {
                    repository.updateSwitchState(swId, ack.isOn)
                }
                repository.updateGatewayLastAck(gateway.id, ack.raw)
            }
        }

        mqttByGatewayId[gateway.gatewayId] = manager
        manager.connect()
    }


    fun toggleSwitch(gateway: SmartGateway, switch: SmartSwitch, on: Boolean) {
        ensureMqttForGateway(gateway)

        val manager = mqttByGatewayId[gateway.gatewayId] ?: return

        if (!manager.isConnected()) {
            Log.e(tag, "Toggle blocked → MQTT not connected (gw=${gateway.gatewayId})")
            // Optimistic UI update even if not connected; pending command will send on connect.
            _uiState.update { old ->
                val gatewayStates = old.switchStates[gateway.gatewayId]?.toMutableMap()
                    ?: mutableMapOf()
                gatewayStates[switch.switchIndex] = on
                val newStates = old.switchStates.toMutableMap()
                newStates[gateway.gatewayId] = gatewayStates
                old.copy(switchStates = newStates)
            }
            return
        }

        Log.d(tag, "Toggle request → gw=${gateway.gatewayId} sw=${switch.switchIndex} on=$on")
        manager.publishSwitchCommand(switch.switchIndex, on)

        // Optimistic UI update; ACK will confirm or correct.
        _uiState.update { old ->
            val gatewayStates = old.switchStates[gateway.gatewayId]?.toMutableMap()
                ?: mutableMapOf()
            gatewayStates[switch.switchIndex] = on
            val newStates = old.switchStates.toMutableMap()
            newStates[gateway.gatewayId] = gatewayStates
            old.copy(switchStates = newStates)
        }
    }
}
