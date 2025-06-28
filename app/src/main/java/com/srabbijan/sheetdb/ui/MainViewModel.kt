package com.srabbijan.sheetdb.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.srabbijan.sheetdb.data.local.entity.DataItem
import com.srabbijan.sheetdb.repository.DataRepository
import com.srabbijan.sheetdb.service.GoogleSignInHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: DataRepository,
    private val signInHelper: GoogleSignInHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    val items = repository.getAllItems()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        checkSignInStatus()
    }

    private fun checkSignInStatus() {
        val user = signInHelper.getCurrentUser()
        _uiState.value = _uiState.value.copy(
            isSignedIn = user != null,
            userEmail = user?.email
        )
    }

    fun handleSignInResult(account: GoogleSignInAccount?) {
        _uiState.value = _uiState.value.copy(
            isSignedIn = account != null,
            userEmail = account?.email
        )
    }

    fun signOut() {
        signInHelper.signOut()
        _uiState.value = _uiState.value.copy(
            isSignedIn = false,
            userEmail = null
        )
    }

    fun addItem(title: String, description: String) {
        viewModelScope.launch {
            val item = DataItem(
                title = title,
                description = description
            )
            repository.insertItem(item)
        }
    }

    fun syncData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            val success = repository.syncToSheet()
            _uiState.value = _uiState.value.copy(
                isSyncing = false,
                syncMessage = if (success) "Sync successful" else "Sync failed"
            )
        }
    }
}

data class MainUiState(
    val isSignedIn: Boolean = false,
    val userEmail: String? = null,
    val isSyncing: Boolean = false,
    val syncMessage: String? = null
)