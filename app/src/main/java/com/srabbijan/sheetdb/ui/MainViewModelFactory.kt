package com.srabbijan.sheetdb.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.srabbijan.sheetdb.repository.DataRepository
import com.srabbijan.sheetdb.service.GoogleSignInHelper

class MainViewModelFactory(
    private val repository: DataRepository,
    private val signInHelper: GoogleSignInHelper
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository, signInHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}