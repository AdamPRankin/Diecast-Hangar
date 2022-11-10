package com.example.diecasthangar.core.util

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.PopupWindow
import com.example.diecasthangar.R

class CommentPopupWindow(
    private val context: Context
) : PopupWindow(context) {
    init {
        val contentView = View.inflate(context, R.layout.popup_add_comment, null)

/*        val addButton = contentView.findViewById<ImageView>(R.id.post_add_comment_button)

        addButton.setOnClickListener {
            dismiss()
        }*/

        setContentView(contentView)

        setBackgroundDrawable(ColorDrawable(0))
    }
}