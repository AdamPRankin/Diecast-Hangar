package com.pingu.diecasthangar.ui.onboarding

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pingu.diecasthangar.data.remote.FirebaseAuthManager
import com.pingu.diecasthangar.data.remote.Response
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AuthenticationViewModel: ViewModel()  {
    private val authManager = FirebaseAuthManager()

    private val userLoggedIn : MutableLiveData<Boolean> = MutableLiveData()
    private val authError: MutableLiveData<String> = MutableLiveData()

    fun getUserState(): MutableLiveData<Boolean> {
        return userLoggedIn
    }

    fun getAuthErrorData(): MutableLiveData<String> {
        return authError
    }

    fun registerUser(username: String, email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            when (val result =authManager.registerUser(email = email, password = password, username = username)){
                is Response.Loading -> {
                }
                is Response.Success -> {
                    userLoggedIn.postValue(true)
                }
                is Response.Failure -> {
                    userLoggedIn.postValue(false)
                    parseError(result)
                    Log.e("AUTH","Error registering: ${result.e}")
                }
            }
        }
    }

    fun loginUser(email: String,password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val auth = Firebase.auth
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    userLoggedIn.postValue(true)
                }
                else if (it.isCanceled){
                    userLoggedIn.postValue(false)
                    authError.postValue("Failed to Log In. Please check password")
                    Log.e("AUTH","Error logging in: $it")
                }
            }
        }
    }

    fun logOutUser(){
        val auth = Firebase.auth
        userLoggedIn.value = false
        auth.signOut()
    }

    private fun parseError(response: Response.Failure){
        authError.postValue(response.e.toString())
        if (response.e == com.google.firebase.auth.FirebaseAuthInvalidCredentialsException("34","The email address is badly formatted")){
            authError.postValue("Credentials were incorrect. Please double check and try again")
        }
    }

}