package com.mr.mukto.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SmartGateway(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gatewayId: String,
    val name: String,
    val lastAck: String? = null
)
