package com.example.diecasthangar.onboarding.presentation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.diecasthangar.NavigationHost
import com.example.diecasthangar.R

class StartFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val loginButton: Button = view.findViewById(R.id.btn_to_login)
        val registerButton: Button = view.findViewById(R.id.btn_to_register)

        loginButton.setOnClickListener {
            (activity as NavigationHost).navigateTo(LoginFragment(), false)
        }

        registerButton.setOnClickListener {
            (activity as NavigationHost).navigateTo(RegistrationFragment(), false)
        }
    }
}