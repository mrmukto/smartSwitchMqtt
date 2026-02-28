package com.mr.mukto.data.local


import androidx.room.*
import com.mr.mukto.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SmartHomeDao {

    @Transaction
    @Query("SELECT * FROM SmartGateway")
    fun getGatewaysWithSwitches(): Flow<List<GatewayWithSwitches>>

    @Insert
    suspend fun insertGateway(gateway: SmartGateway): Long

    @Update
    suspend fun updateGateway(gateway: SmartGateway)

    @Query("UPDATE SmartGateway SET lastAck = :lastAck WHERE id = :gatewayDbId")
    suspend fun updateGatewayLastAck(gatewayDbId: Long, lastAck: String)

    @Insert
    suspend fun insertSwitches(items: List<SmartSwitch>)   // FIX

    @Update
    suspend fun updateSwitchItem(item: SmartSwitch)

    @Query("UPDATE SmartSwitch SET is_on = :isOn WHERE id = :switchId")
    suspend fun updateSwitchState(switchId: Int, isOn: Boolean)

    @Delete
    suspend fun deleteGateway(gateway: SmartGateway)
// FIX
}
