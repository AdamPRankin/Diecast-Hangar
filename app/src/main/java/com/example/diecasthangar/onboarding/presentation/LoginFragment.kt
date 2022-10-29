package com.example.diecasthangar.onboarding.presentation

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.diecasthangar.DashboardFragment
import com.example.diecasthangar.R
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val cancelButton = view.findViewById<Button>(R.id.btn_cancel_login)
        val loginButton = view.findViewById<Button>(R.id.login_button)
        val passwordField = view.findViewById<EditText>(R.id.login_password_edit_text)
        val passwordInput = view.findViewById<TextInputLayout>(R.id.login_password_text_input)
        val emailField = view.findViewById<EditText>(R.id.login_email_edit_text)
        val emailInput = view.findViewById<TextInputLayout>(R.id.login_email_text_input)

        val recoverPasswordTextView: TextView = view.findViewById(R.id.recover_password)

        recoverPasswordTextView.setOnClickListener {
            showRecoverPasswordDialog()
        }

        // Set an error if the password is less than 8 characters.
        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString()
            loginUser(email,password,passwordInput)
        }

        // Clear the error once more than 8 characters are typed.
        passwordField.setOnKeyListener { _, _, _ ->
            if (isPasswordValid(passwordField.text!!)) {
                // Clear the error.
                passwordField.error = null
            }
            false
        }

        cancelButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, StartFragment())
                .commit()
        }

        return view
    }

    private fun loginUser(email: String, password: String, textInputLayout: TextInputLayout){
        val auth: FirebaseAuth = Firebase.auth
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.container, DashboardFragment())
                        .commit()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    textInputLayout.error = "could not log in"

                }
            }
    }

    private fun isPasswordValid(text: Editable?): Boolean {
        return (text != null) && (text.length >= 6)
    }

    private fun showRecoverPasswordDialog(){
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

    private fun beginRecovery(email: String){
        val auth: FirebaseAuth = Firebase.auth
        auth.sendPasswordResetEmail(email).addOnCompleteListener {

            @Override
            fun onComplete(e: java.lang.Exception) {
               Toast.makeText(view?.context,"Email sent",Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {

            @Override
            fun onFailure(e: java.lang.Exception) {
                Toast.makeText(view?.context,"not sent",Toast.LENGTH_SHORT).show()
            }
        }

    }
}