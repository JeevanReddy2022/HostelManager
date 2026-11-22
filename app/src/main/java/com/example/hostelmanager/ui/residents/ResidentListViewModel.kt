package com.example.hostelmanager.ui.residents

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hostelmanager.data.AppDatabase
import com.example.hostelmanager.data.ResidentWithRoom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

/**
 * Search across name, room number, Govt ID, phone and emergency contact.
 * Order stays alphabetical by name (comes from the database query).
 */
internal fun searchResidents(
    residents: List<ResidentWithRoom>,
    rawQuery: String
): List<ResidentWithRoom> {
    val q = rawQuery.trim()
    if (q.isEmpty()) return residents

    return residents.filter { r ->
        r.person.name.contains(q, ignoreCase = true) ||
                r.roomNumber.contains(q, ignoreCase = true) ||
                r.person.govtIdNumber.contains(q, ignoreCase = true) ||
                r.person.phone.contains(q) ||
                r.person.emergencyContact.contains(q)
    }
}

class ResidentListViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.personDao()

    private val _hostelId = MutableStateFlow("")

    val residents: StateFlow<List<ResidentWithRoom>> = _hostelId.flatMapLatest { id ->
        if (id.isEmpty()) flowOf(emptyList())
        else dao.getActiveResidentsByHostel(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Lives in the ViewModel so the search survives tab switches and rotation.
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    fun setHostelId(id: String) {
        if (_hostelId.value != id) {
            _hostelId.value = id
            _query.value = ""
        }
    }

    fun setQuery(value: String) {
        _query.value = value.take(40)
    }

    fun clearSearch() {
        _query.value = ""
    }
}