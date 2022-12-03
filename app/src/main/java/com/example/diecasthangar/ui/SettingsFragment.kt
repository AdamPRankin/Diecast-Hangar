package com.example.diecasthangar.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import com.example.diecasthangar.R
import com.example.diecasthangar.databinding.FragmentSettingsBinding
import com.example.diecasthangar.ui.dashboard.DashboardFragment
import com.example.diecasthangar.ui.dashboard.DashboardViewModel
import com.example.diecasthangar.ui.onboarding.AuthenticationViewModel
import com.example.diecasthangar.ui.onboarding.OnboardingFragment

class SettingsFragment: Fragment(), LifecycleOwner {
    private val authViewModel by viewModels<AuthenticationViewModel>()
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val view = binding.root

        val logoutButton = binding.btnLogout
        val themesButton = binding.btnThemes

        val darkModeOnButton = binding.darkModeOn
        val darkModeOffButton = binding.darkModeOff
        val darkModeAutoButton = binding.darkModeAuto

        val themesMenu = binding.themesMenu

        val darkButtonToggle = binding.darkModeToggleButton

        logoutButton.setOnClickListener {
            authViewModel.logOutUser()
        }

        themesButton.setOnClickListener {
            if (themesMenu.visibility == View.VISIBLE){
                themesMenu.visibility = View.GONE
            }
            else {
                themesMenu.visibility = View.VISIBLE
            }
        }
        val sharedPreference =  requireActivity().getSharedPreferences("DIECAST_HANGAR", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        binding.buttonDynamicColors.setOnClickListener {
            //set dynamic (default)
            //set dark mode based on togglerino
        }

        binding.buttonOrangeCrush.setOnClickListener {
            editor.putString("THEME","orange crush").apply()

            val intent = requireActivity().intent
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            requireActivity().finish()
            startActivity(intent)
        }

        //hightlight current selected option for night mode
        when (getDefaultNightMode()) {
            MODE_NIGHT_YES -> {
                darkButtonToggle.check(R.id.dark_mode_on)
            }
            MODE_NIGHT_NO -> {
                darkButtonToggle.check(R.id.dark_mode_off)
            }
            MODE_NIGHT_FOLLOW_SYSTEM -> {
                darkButtonToggle.check(R.id.dark_mode_auto)
            }
            MODE_NIGHT_AUTO_BATTERY -> {
                darkButtonToggle.check(R.id.dark_mode_auto)
            }
            MODE_NIGHT_AUTO_TIME -> {
                darkButtonToggle.check(R.id.dark_mode_auto)
            }
            MODE_NIGHT_UNSPECIFIED -> {
                darkButtonToggle.clearChecked()
            }
        }
        binding.buttonSpicyToothpaste.setOnClickListener {

            editor.putString("THEME","cinnamon").apply()

            val intent = requireActivity().intent
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            requireActivity().finish()
            startActivity(intent)
        }

        darkModeAutoButton.setOnClickListener {
            darkButtonToggle.clearChecked()
            darkButtonToggle.check(R.id.dark_mode_auto)
            editor.putString("DARK_MODE","auto").apply()
            setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
        }

        darkModeOnButton.setOnClickListener {
            //workaround for bug in material 3 toggle button group implementation involving activity restart
            darkButtonToggle.clearChecked()
            darkButtonToggle.check(R.id.dark_mode_on)
            editor.putString("DARK_MODE","on").apply()
            setDefaultNightMode(MODE_NIGHT_YES)
        }

        darkModeOffButton.setOnClickListener {
            darkButtonToggle.clearChecked()
            darkButtonToggle.check(R.id.dark_mode_off)
            editor.putString("DARK_MODE","off").apply()
            setDefaultNightMode(MODE_NIGHT_NO)
        }

        authViewModel.getUserState().observe(viewLifecycleOwner) {
            if (it) {
                //abstain from action
            }
            else{
                parentFragmentManager.popBackStack()
                parentFragmentManager.beginTransaction()
                    .add(R.id.container, OnboardingFragment()).remove(this)
                    .commit()
            }
        }


        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}