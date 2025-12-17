package com.mrm.amarisca.data.local


import androidx.room.*
import com.mrm.amarisca.data.local.entities.*
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

    @Insert
    suspend fun insertSwitches(items: List<SmartSwitch>)   // FIX

    @Update
    suspend fun updateSwitchItem(item: SmartSwitch)
    @Delete
    suspend fun deleteGateway(gateway: SmartGateway)
// FIX
}
