package com.mr.mukto.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mr.mukto.data.local.SmartHomeDatabase
import com.mr.mukto.data.repository.SmartHomeRepository

class SmartHomeVMFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(SmartHomeViewModel::class.java)) {

            // Build Room DB
            val db = SmartHomeDatabase.getInstance(context)

            // Build repository
            val repo = SmartHomeRepository(db.smartHomeDao())

            @Suppress("UNCHECKED_CAST")
            return SmartHomeViewModel(repo) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
