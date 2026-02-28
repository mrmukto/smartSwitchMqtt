package com.mr.mukto.data.local.entities


import androidx.room.Embedded
import androidx.room.Relation

data class GatewayWithSwitches(
    @Embedded
    var gateway: SmartGateway? = null,

    @Relation(
        parentColumn = "id",
        entityColumn = "gatewayId"
    )
    var switches: List<SmartSwitch> = emptyList()
)
