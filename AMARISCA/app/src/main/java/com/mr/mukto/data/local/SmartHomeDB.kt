package com.mr.mukto.data.local


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mr.mukto.data.local.entities.SmartGateway
import com.mr.mukto.data.local.entities.SmartSwitch

@Database(
    entities = [SmartGateway::class, SmartSwitch::class],
    version = 4,
    exportSchema = false
)
abstract class SmartHomeDatabase : RoomDatabase() {

    abstract fun smartHomeDao(): SmartHomeDao

    companion object {
        @Volatile private var INSTANCE: SmartHomeDatabase? = null

        fun getInstance(context: Context): SmartHomeDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    SmartHomeDatabase::class.java,
                    "smart_home.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
