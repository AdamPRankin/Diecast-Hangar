package com.pingu.diecasthangar.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import com.pingu.diecasthangar.R
import com.pingu.diecasthangar.databinding.FragmentSettingsBinding
import com.pingu.diecasthangar.ui.onboarding.AuthenticationViewModel
import com.pingu.diecasthangar.ui.onboarding.OnboardingFragment

class SettingsFragment: Fragment(), LifecycleOwner {
    private val authViewModel by viewModels<AuthenticationViewModel>()
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private var themesOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null){
            themesOpen = savedInstanceState.getBoolean("themes")
        }
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val view = binding.root

        val logoutButton = binding.btnLogout
        val themesButton = binding.btnThemes

        val darkButtonToggle = binding.darkModeToggleButton
        val darkModeOnButton = binding.darkModeOn
        val darkModeOffButton = binding.darkModeOff
        val darkModeAutoButton = binding.darkModeAuto

        val themesMenu = binding.themesMenu
        val languageMenu = binding.languageMenu

        val languageButton = binding.btnLanguage
        val englishButton = binding.languageEnglish
        val ukrainianButton = binding.languageUkrainian
        val gaelicButton = binding.languageGaelic

        logoutButton.setOnClickListener {
            authViewModel.logOutUser()
        }

        if (themesOpen){
            themesMenu.visibility = View.VISIBLE
        }

        themesButton.setOnClickListener {
            if (themesMenu.visibility == View.VISIBLE){
                themesMenu.visibility = View.GONE
                themesOpen = false
            }
            else {
                themesMenu.visibility = View.VISIBLE
                themesOpen = true
            }
        }
        val sharedPreference =  requireActivity().getSharedPreferences("DIECAST_HANGAR", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        binding.buttonDynamicColors.setOnClickListener {
            editor.putString("THEME","dynamic").apply()
            val intent = requireActivity().intent
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            requireActivity().finish()
            startActivity(intent)
        }

        binding.buttonOrangeCrush.setOnClickListener {
            editor.putString("THEME","orange crush").apply()

            val intent = requireActivity().intent
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            requireActivity().finish()
            startActivity(intent)
        }
        binding.buttonSpicyToothpaste.setOnClickListener {

            editor.putString("THEME","cinnamon").apply()

            val intent = requireActivity().intent
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            requireActivity().finish()
            startActivity(intent)
        }
        binding.buttonRoyal.setOnClickListener {

            editor.putString("THEME","royal").apply()

            val intent = requireActivity().intent
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            requireActivity().finish()
            startActivity(intent)
        }

        //highlight current selected option for night mode
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

        darkModeAutoButton.setOnClickListener {
            darkButtonToggle.clearChecked()
            darkButtonToggle.check(R.id.dark_mode_auto)
            setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
            editor.putString("DARK_MODE","auto").apply()

        }

        darkModeOnButton.setOnClickListener {
            //workaround for bug in material 3 toggle button group implementation involving activity restart
            darkButtonToggle.clearChecked()
            darkButtonToggle.check(R.id.dark_mode_on)
            setDefaultNightMode(MODE_NIGHT_YES)
            editor.putString("DARK_MODE","on").apply()

        }

        darkModeOffButton.setOnClickListener {
            darkButtonToggle.clearChecked()
            darkButtonToggle.check(R.id.dark_mode_off)
            setDefaultNightMode(MODE_NIGHT_NO)
            editor.putString("DARK_MODE","off").apply()
        }

/*        languageButton.setOnClickListener {
            if (languageMenu.visibility == View.VISIBLE){
                languageMenu.visibility = View.GONE
            }
            else {
                languageMenu.visibility = View.VISIBLE
            }
        }*/

        englishButton.setOnClickListener {

        }
        gaelicButton.setOnClickListener {


        }
        ukrainianButton.setOnClickListener {

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


    override fun onSaveInstanceState(outState: Bundle) {
        //save UI state
        outState.putBoolean("themes", themesOpen)
        super.onSaveInstanceState(outState)
        //outState.putString("avatar-uri", )

    }
}