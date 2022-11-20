package com.example.diecasthangar.ui.onboarding

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
import com.example.diecasthangar.core.util.NavigationHost
import com.example.diecasthangar.R
import com.example.diecasthangar.R.string
import com.example.diecasthangar.databinding.FragmentRegisterBinding
import com.example.diecasthangar.data.remote.FirestoreRepository
import com.example.diecasthangar.ui.DashboardFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

class RegistrationFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val view = binding.root
        // Inflate the layout for this fragment
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
            val storage: FirebaseStorage = FirebaseStorage.getInstance()
            val db: FirebaseFirestore = Firebase.firestore
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(it) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                        val user = Firebase.auth.currentUser
                        val profileUpdates = userProfileChangeRequest {
                            val usernameField = view?.findViewById<EditText>(R.id.reg_username_edit_text)
                            val displayName = usernameField!!.text.toString()
                            val repository = FirestoreRepository(storage, db)
                            repository.addUserInfoToDatabase(user!!.uid,"",displayName)
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
                        val snackbar = Snackbar.make(requireView(), "failed to register user", Snackbar.LENGTH_LONG)
                                snackbar.show()
                    }
                }
        }
    }

    private fun isPasswordValid(text: Editable?): Boolean {
        return text != null && text.length >= 6
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}