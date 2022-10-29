package com.example.diecasthangar.onboarding.presentation

import android.content.ContentValues.TAG
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.diecasthangar.DashboardFragment
import com.example.diecasthangar.NavigationHost
import com.example.diecasthangar.R
import com.example.diecasthangar.R.layout
import com.example.diecasthangar.R.string
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase

class RegistrationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(layout.fragment_register, container, false)
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

        val cancelButton = view.findViewById<Button>(R.id.btn_cancel_register)
        val registerButton = view.findViewById<Button>(R.id.btn_register)
        val passwordField = view.findViewById<EditText>(R.id.reg_password_edit_text)
        val passwordInput = view.findViewById<TextInputLayout>(R.id.reg_password_text_input)
        val emailInput = view.findViewById<TextInputLayout>(R.id.reg_email_text_input)
        val emailField = view.findViewById<EditText>(R.id.reg_email_edit_text)
        val usernameInput = view.findViewById<TextInputLayout>(R.id.reg_username_text_input)
        val usernameField = view.findViewById<EditText>(R.id.reg_username_edit_text)

        // Set an error if the password is less than 8 characters.
        registerButton.setOnClickListener {
            if (!isPasswordValid(passwordField.text!!)) {
                passwordInput.error = getString(string.invalid_password)
            } else {
                // Clear the error.
                passwordInput.error = null
                // Navigate to the next Fragment.
                (activity as NavigationHost).navigateTo(DashboardFragment(), false)
            }
        }

        // Clear the error once more than 8 characters are typed.
        passwordField.setOnKeyListener { _, _, _ ->
            if (isPasswordValid(passwordField.text!!)) {
                // Clear the error.
                passwordField.error = null
            }
            false
        }

        registerButton.setOnClickListener {
            val email: String  = emailField.text.toString().trim()
            val password: String = passwordField.text.toString()
            val username: String = usernameField.text.toString()

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                emailInput.error = "invalid email"
            }
            else {
                registerUser(email,password,mAuth)

                parentFragmentManager.beginTransaction()
                    .replace(R.id.container, DashboardFragment())
                    .commit()
            }

        }
        cancelButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, StartFragment())
                .commit()
        }

        return view
    }

    private fun registerUser(email: String, password: String,mAuth: FirebaseAuth) {
        activity?.let {
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(it) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = Firebase.auth.currentUser
                        val profileUpdates = userProfileChangeRequest {
                            val usernameField = view?.findViewById<EditText>(R.id.reg_username_edit_text)
                            displayName = usernameField!!.text.toString()
                        }
                        user!!.updateProfile(profileUpdates).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "User profile updated.")
                            }
                        }
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.container, DashboardFragment())
                            .commit()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        //TODO display this
                    }
                }
        }



    }

    private fun isPasswordValid(text: Editable?): Boolean {
        return text != null && text.length >= 6
    }
}