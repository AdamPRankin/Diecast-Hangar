package com.example.diecasthangar

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment


class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        // Inflate the layout for this fragment

        val saveProfileButton: ImageButton = view.findViewById(R.id.btn_profile_save)
        saveProfileButton.visibility = View.GONE

        val profileImage: ImageView = view.findViewById(R.id.profile_avatar)

        val profileUsername: TextView = view.findViewById(R.id.profile_name)

        val launcher = registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK
                && result.data != null
            ) {
                val photoUri: Uri? = result.data!!.data
                profileImage.setImageURI(photoUri)
                saveProfileButton.visibility = View.VISIBLE
            }
        }
        profileImage.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            launcher.launch(intent)
        }

        saveProfileButton.setOnClickListener {
            val username = profileUsername.text.toString()
            val photo = profileImage.id
        }

        return view
    }


    override fun onPause() {
        super.onPause()
        //TODO save profile data

    }

}