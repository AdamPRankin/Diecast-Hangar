package com.example.diecasthangar.ui.onboarding

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.diecasthangar.R
import com.example.diecasthangar.data.remote.FirebaseAuthManager
import com.example.diecasthangar.databinding.FragmentOboardingBinding
import com.example.diecasthangar.ui.dashboard.DashboardFragment
import com.example.diecasthangar.ui.UserViewModel
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase

class OnboardingFragment : Fragment() {
    private var _binding: FragmentOboardingBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val userViewModel: UserViewModel by activityViewModels()

    private val authViewModel: AuthenticationViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentOboardingBinding.inflate(inflater, container, false)
        val view = binding.root
        // Inflate the layout for this fragment

        val registerButton = binding.btnRegister
        val registerPasswordField = binding.regPasswordEditText
        val registerEmailEditText = binding.regEmailEditText
        val registerUsernameEditText = binding.regUsernameEditText
        val toLoginText = binding.textToLogin
        val toRegisterText = binding.textToRegister
        val loginLayout = binding.layoutLogin
        val registerLayout = binding.layoutRegister
        val loginButton = binding.loginButton
        val loginPasswordField = binding.loginPasswordEditText
        val loginEmailField = binding.loginEmailEditText
        val recoverPasswordTextView: TextView = binding.recoverPassword

        toLoginText.setOnClickListener {
            registerLayout.visibility = View.GONE
            loginLayout.visibility = View.VISIBLE
        }

        toRegisterText.setOnClickListener {
            registerLayout.visibility = View.VISIBLE
            loginLayout.visibility = View.GONE
        }


        registerButton.setOnClickListener {
            val email: String  = registerEmailEditText.text.toString()
            val password: String = registerPasswordField.text.toString()
            val username: String = registerUsernameEditText.text.toString()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(context,"Invalid Email",LENGTH_SHORT).show()
            }
            else{
                authViewModel.registerUser(username,email,password)
            }

        }

        recoverPasswordTextView.setOnClickListener {
            showRecoverPasswordDialog()
        }

        // Set an error if the password is less than 8 characters.
        loginButton.setOnClickListener {
            val email = loginEmailField.text.toString().trim()
            val password = loginPasswordField.text.toString()

            authViewModel.loginUser(email, password)

        }

        authViewModel.getUserState().observe(viewLifecycleOwner) {
            if (it) {
                // we have successfully logged in or registered, so go to dash
                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, DashboardFragment()).remove(this)
                    .commit()
            }
            else{
                Toast.makeText(view.context,"Authentication failed, please try again",
                    Toast.LENGTH_SHORT).show()
            }
        }

        authViewModel.getAuthErrorData().observe(viewLifecycleOwner) {

                Toast.makeText(context, it, LENGTH_SHORT).show()

        }
        return view
    }

    private fun showRecoverPasswordDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(view?.context)
        val layout = LinearLayout(view?.context)
        builder.setTitle("reset password")
        val emailText = EditText(view?.context)
        emailText.hint = "email"
        emailText.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        emailText.minEms = 10

        layout.addView(emailText)
        layout.setPadding(10,10,10,10)

        builder.setView(layout)
        builder.setPositiveButton("send") { dialog, which ->
            val email: String = emailText.text.toString().trim()
            beginRecovery(email)
        }
        builder.setNegativeButton("cancel") { dialog, which ->
            dialog.dismiss()
        }.create().show()
    }

    //todo authmanager/viewmodel
    private fun beginRecovery(email: String){
        val auth: FirebaseAuth = Firebase.auth
        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            Toast.makeText(view?.context,"Email sent", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(view?.context,"failed to send, please try again later", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}