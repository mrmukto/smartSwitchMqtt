package com.mr.mukto.data.repository


import com.mr.mukto.data.local.SmartHomeDao
import com.mr.mukto.data.local.entities.*
import kotlinx.coroutines.flow.Flow

class SmartHomeRepository(private val dao: SmartHomeDao) {

    fun getGatewaysWithSwitches(): Flow<List<GatewayWithSwitches>> =
        dao.getGatewaysWithSwitches()

    suspend fun addGatewayWithDefaultSwitches(
        gatewayId: String,
        name: String
    ) {
        val gateway = SmartGateway(
            gatewayId = gatewayId,
            name = name.ifBlank { "Gateway $gatewayId" },
            lastAck = null
        )

        val newGatewayId = dao.insertGateway(gateway)  // returns LONG PK

        // Create 4 default switches
        val switches = (1..4).map { index ->
            SmartSwitch(
                gatewayId = newGatewayId,
                switchIndex = index,
                switchName = "Switch $index",
                switchType = "Light",
                isOn = false
            )
        }

        dao.insertSwitches(switches)   // safe list insert
    }

    suspend fun updateGateway(gateway: SmartGateway, newName: String, newGatewayId: String) {
        val updated = gateway.copy(
            name = newName,
            gatewayId = newGatewayId
        )
        dao.updateGateway(updated)
    }

    suspend fun deleteGateway(gateway: SmartGateway) {
        dao.deleteGateway(gateway)
    }


    suspend fun updateSwitch(
        switch: SmartSwitch,
        newName: String,
        newType: String
    ) {
        dao.updateSwitchItem(
            switch.copy(
                switchName = newName,
                switchType = newType
            )
        )
    }

    suspend fun updateSwitchState(switchId: Int, isOn: Boolean) {
        dao.updateSwitchState(switchId, isOn)
    }

    suspend fun updateGatewayLastAck(gatewayDbId: Long, lastAck: String) {
        dao.updateGatewayLastAck(gatewayDbId, lastAck)
    }
}
