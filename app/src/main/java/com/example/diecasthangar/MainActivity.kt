package com.example.diecasthangar

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.diecasthangar.domain.Response
import com.example.diecasthangar.domain.remote.FirestoreRepository
import com.example.diecasthangar.domain.usecase.remote.getUser
import com.example.diecasthangar.onboarding.presentation.StartFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), NavigationHost {
    var username : String = "test"
    var avatarUri: String = "none"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val db: FirebaseFirestore = Firebase.firestore
        val repository = FirestoreRepository(storage,db)


        val currentUser = Firebase.auth.currentUser
        if(currentUser != null){
            lifecycleScope.launch {

                when(val response = repository.getUserInfo(currentUser.uid)) {
                    is Response.Loading -> {
                    }
                    is Response.Success -> {
                        val (avatar,user) = response.data!!
                        avatarUri = avatar
                        username = user
                        supportFragmentManager
                            .beginTransaction()
                            .add(R.id.container, DashboardFragment())
                            .commit()
                    }
                    is Response.Failure -> {
                        print(response.e)
                    }
                }
            }
        }
        else {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.container, StartFragment())
                .commit()
        }
    }
    override fun navigateTo(fragment: Fragment, addToBackstack: Boolean) {
        val transaction = supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, fragment)

        if (addToBackstack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }
}