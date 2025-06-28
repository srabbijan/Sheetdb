package com.srabbijan.sheetdb

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.srabbijan.sheetdb.data.local.AppDatabase
import com.srabbijan.sheetdb.repository.DataRepository
import com.srabbijan.sheetdb.service.GoogleSheetsService
import com.srabbijan.sheetdb.service.GoogleSignInHelper
import com.srabbijan.sheetdb.ui.MainScreen
import com.srabbijan.sheetdb.ui.MainViewModel
import com.srabbijan.sheetdb.ui.MainViewModelFactory
import com.srabbijan.sheetdb.ui.theme.SheetdbTheme
import com.srabbijan.sheetdb.worker.SyncScheduler

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var signInHelper: GoogleSignInHelper

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                viewModel.handleSignInResult(account)
            } catch (e: ApiException) {
                viewModel.handleSignInResult(null)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupDependencies()

        setContent {
            SheetdbTheme {
                MainScreen(
                    viewModel = viewModel,
                    onSignInClick = {
                        val signInIntent = signInHelper.getSignInIntent()
                        signInLauncher.launch(signInIntent)
                    }
                )
            }
        }

        // Schedule periodic sync
        SyncScheduler(this).schedulePeriodicSync()
    }

    private fun setupDependencies() {
        val database = AppDatabase.getDatabase(this)
        val sheetsService = GoogleSheetsService(this)
        signInHelper = GoogleSignInHelper(this)
        val repository = DataRepository(database.dataItemDao(), sheetsService, this)

        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(repository, signInHelper)
        )[MainViewModel::class.java]
    }
}