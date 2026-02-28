package com.mr.mukto.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SmartSwitch(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gatewayId: Long,                         // MUST MATCH parentColumn
    @ColumnInfo(name = "switch_index") val switchIndex: Int,
    val switchName: String,
    val switchType: String,
    @ColumnInfo(name = "is_on") val isOn: Boolean = false
)
