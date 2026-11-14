package com.example.hostelmanager.ui.hostels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.AppDatabase
import com.example.hostelmanager.data.Hostel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HostelViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.hostelDao()

    // Live list of active hostels, automatically updates when DB changes
    val hostels: StateFlow<List<Hostel>> = dao.getAllActiveHostels()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addHostel(name: String, address: String, phone: String) {
        viewModelScope.launch {
            dao.insertHostel(
                Hostel(
                    name = name,
                    address = address,
                    phone = phone
                )
            )
        }
    }
}