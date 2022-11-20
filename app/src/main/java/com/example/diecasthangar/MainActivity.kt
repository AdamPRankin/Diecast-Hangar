package com.example.diecasthangar

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.diecasthangar.core.util.NavigationHost
import com.example.diecasthangar.ui.DashboardFragment
import com.example.diecasthangar.ui.onboarding.StartFragment
import com.example.diecasthangar.ui.UserViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

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