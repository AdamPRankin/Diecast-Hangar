package com.example.diecasthangar

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.diecasthangar.core.util.NavigationHost
import com.example.diecasthangar.ui.UserViewModel
import com.example.diecasthangar.ui.dashboard.DashboardFragment
import com.example.diecasthangar.ui.dashboard.DashboardViewModel
import com.example.diecasthangar.ui.onboarding.OnboardingFragment
import com.google.android.gms.common.util.DeviceProperties.isTablet
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity(), NavigationHost {
    private val userViewModel: UserViewModel by viewModels()
    val dashViewModel: DashboardViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreference =  getSharedPreferences("DIECAST_HANGAR", Context.MODE_PRIVATE)

        //get saved theme preference
        when (sharedPreference.getString("THEME","default theme")) {
            "dynamic" -> theme.applyStyle(R.style.Base_Theme_DiecastHangar, true)
            "orange crush" -> theme.applyStyle(R.style.Mango_Theme_DiecastHangar, true)
            "cinnamon" -> theme.applyStyle(R.style.Cinnamon_Theme_DiecastHangar, true)
            "royal" -> theme.applyStyle(R.style.Royal_Theme_DiecastHangar, true)
            else -> {
                //default theme
                theme.applyStyle(R.style.Mango_Theme_DiecastHangar, true)
            }
        }

        //get saved dark mode preference
        when (sharedPreference.getString("DARK_MODE","auto")) {
            "auto" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            "on" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "off" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

/*        val localeString = sharedPreference.getString("LOCALE","en")
        val locale = Locale("en")
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        applicationContext.resources.updateConfiguration(config, null)*/

        val currentUser = Firebase.auth.currentUser
        val dashContainer =
            if (isTablet(this)){
            R.id.left_container
        } else {
            R.id.container
        }

        if (savedInstanceState == null){
            if(currentUser != null){
                userViewModel.getUserData()
                supportFragmentManager
                    .beginTransaction()
                    .add(dashContainer, DashboardFragment())
                    .commit()
            }
            else {
                supportFragmentManager
                    .beginTransaction()
                    .add(dashContainer, OnboardingFragment())
                    .commit()
            }
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