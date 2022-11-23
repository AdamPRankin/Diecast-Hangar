package com.example.diecasthangar.ui.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.diecasthangar.core.util.NavigationHost
import com.example.diecasthangar.R
import com.example.diecasthangar.databinding.FragmentStartBinding

class StartFragment : Fragment() {

    private var _binding: FragmentStartBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}