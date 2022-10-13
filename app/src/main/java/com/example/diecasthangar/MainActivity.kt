package com.example.diecasthangar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.diecasthangar.core.UserController
import com.example.diecasthangar.onboarding.presentation.StartFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity(), NavigationHost {
    val userController: UserController = UserController(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val auth: FirebaseAuth = Firebase.auth
        val currentUser = auth.currentUser
        if(currentUser != null){
           userController.currentUser = currentUser
            supportFragmentManager
                .beginTransaction()
                .add(R.id.container, DashboardFragment())
                .commit()
        }

        if (savedInstanceState == null) {
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