package com.example.diecasthangar

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.diecasthangar.domain.Response
import com.example.diecasthangar.domain.remote.FirestoreRepository
import com.example.diecasthangar.onboarding.presentation.StartFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), NavigationHost {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val userViewModel: UserViewModel by viewModels()

        val currentUser = Firebase.auth.currentUser
        if(currentUser != null){
            userViewModel.getUserData()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.container, DashboardFragment())
                .commit()
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