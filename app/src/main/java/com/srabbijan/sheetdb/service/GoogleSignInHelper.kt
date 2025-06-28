package com.srabbijan.sheetdb.service

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope

class GoogleSignInHelper(private val context: Context) {
    private val googleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(
                Scope("https://www.googleapis.com/auth/drive.file"),
                Scope("https://www.googleapis.com/auth/spreadsheets")
            )
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(): Intent = googleSignInClient.signInIntent

    fun signOut() = googleSignInClient.signOut()

    fun getCurrentUser(): GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)
}