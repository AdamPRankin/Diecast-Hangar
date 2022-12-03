package com.example.diecasthangar

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.diecasthangar.core.util.NavigationHost
import com.example.diecasthangar.ui.dashboard.DashboardFragment
import com.example.diecasthangar.ui.UserViewModel
import com.example.diecasthangar.ui.onboarding.OnboardingFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity(), NavigationHost {
    private val userViewModel: UserViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreference =  getSharedPreferences("DIECAST_HANGAR", Context.MODE_PRIVATE)

        //get saved theme preference
        when (sharedPreference.getString("THEME","default theme")) {
            "dynamic" -> theme.applyStyle(R.style.Base_Theme_DiecastHangar, true)
            "orange crush" -> theme.applyStyle(R.style.Mango_Theme_DiecastHangar, true)
            "cinnamon" -> theme.applyStyle(R.style.Cinnamon_Theme_DiecastHangar, true)
            else -> {

            }
        }

        //get saved dark mode preference
        when (sharedPreference.getString("DARK_MODE","auto")) {
            "auto" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            "on" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "off" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }


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
                .add(R.id.container, OnboardingFragment())
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