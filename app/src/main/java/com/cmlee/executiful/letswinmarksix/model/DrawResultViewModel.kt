package com.cmlee.executiful.letswinmarksix.model

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cmlee.executiful.letswinmarksix.UserPreference
import com.cmlee.executiful.letswinmarksix.model.tickets.Ticket
import com.cmlee.executiful.letswinmarksix.roomdb.DrawResult
import com.cmlee.executiful.letswinmarksix.roomdb.DrawResultRepository
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.core.content.edit

class DrawResultViewModel(private val sharedFile: SharedPreferences, private val drawResultRepository: DrawResultRepository) : ViewModel() {
//    private val repository = DrawResultRepository(M6Db.getDatabase(application).DrawResultDao())
    private var _results = listOf<DrawResult>()
    val results:List<DrawResult> get() = _results
    var isLoading = false

    private val gson = GsonBuilder().create()
    private val _preferences = mutableStateListOf<UserPreference>()
    val preferences: SnapshotStateList<UserPreference> = _preferences
    fun loadPreferences() {
        _preferences.clear()
        _preferences.addAll(sharedFile.all.map{(key, value) -> UserPreference(key, gson.fromJson(value.toString(),
            Ticket::class.java)) })
    }

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()
    private val _showImageDialog = MutableStateFlow(false)
    val showImageDialog:StateFlow<Boolean> = _showImageDialog.asStateFlow()
    private val _selectedDetail = MutableStateFlow<List<DrawResult>>(emptyList())
    val selectedDetail: StateFlow<List<DrawResult>> = _selectedDetail.asStateFlow()

    private val _index = MutableStateFlow(-1)
    val index : StateFlow<Int> = _index.asStateFlow()
    // Simulate database call to get details based on preference key
    fun getDetailByPreferenceKey(pref: UserPreference) {
        _index.value = _preferences.indexOf(pref)
        viewModelScope.launch{
            _loading.value = true

            // Simulate network/database call
            val mockDetails = drawResultRepository.checkDrawBy(pref.value.drawYear, pref.value.drawNo, pref.value.draws)

            _selectedDetail.value = mockDetails
            _loading.value = false
        }
    }
    fun check(year: String, code: String, draws: Int = 1) {
        viewModelScope.launch {
            if (!isLoading) {
                isLoading = true
                _results = drawResultRepository.checkDrawBy(year, code, draws)
                Log.d("VM", "${results.size}")
            }
            isLoading = false
        }
    }

    fun showDeleteConfirmation() {
        _showDeleteDialog.value = true
    }

    fun showTicketImage(){
        _showImageDialog.value=true
    }

    fun cancelShowImage(){
        _showImageDialog.value=false
    }
    fun deleteSelectedPreference() {
        if(_selectedDetail.value.isNotEmpty()) {
            // Remove from SharedPreferences
            sharedFile.edit { remove(_preferences[index.value].key) }
            loadPreferences()
            // Remove from local list
//            _preferences.removeAll { it.key == currentDetail.preferenceKey }
            // Clear selected detail
            _selectedDetail.value = emptyList()
        }
        _showDeleteDialog.value = false
    }

    fun cancelDelete() {
        _showDeleteDialog.value = false
    }

    fun clearSelectedDetail() {
        _selectedDetail.value = emptyList()
    }
}
class DrawResultViewModelFactory(private val sharedFile: SharedPreferences, private val drawResultRepository: DrawResultRepository):
    ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DrawResultViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DrawResultViewModel(sharedFile, drawResultRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}