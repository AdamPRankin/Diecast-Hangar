package com.example.diecasthangar

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView


class ViewPostFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_post, container, false)

        val dateTextView: TextView = view.findViewById(R.id.view_post_date)
        val avatarImageView: ImageView = view.findViewById(R.id.view_post_avatar)
        val bodyTextView: TextView = view.findViewById(R.id.view_post_body)
        val userTextView: TextView = view.findViewById(R.id.view_post_username)
        val picImageview: ImageView = view.findViewById(R.id.view_post_picture)
        val postImageHolder: LinearLayout = view.findViewById(R.id.view_post_photo_container)
        val leftImageButton: Button = view.findViewById(R.id.btn_view_post_img_left)
        val rightImageButton: Button = view.findViewById(R.id.btn_view_post_img_right)
        return view
    }


}