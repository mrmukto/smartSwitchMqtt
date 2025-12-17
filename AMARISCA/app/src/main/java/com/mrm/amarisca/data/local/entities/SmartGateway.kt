package com.mrm.amarisca.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SmartGateway(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gatewayId: String,
    val name: String
)
