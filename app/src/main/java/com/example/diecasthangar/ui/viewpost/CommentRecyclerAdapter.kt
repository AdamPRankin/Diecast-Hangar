package com.example.diecasthangar.ui.viewpost

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.PopupWindow
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.diecasthangar.core.util.parseDate
import com.example.diecasthangar.data.model.Comment
import com.example.diecasthangar.data.remote.getUser
import com.example.diecasthangar.databinding.PopupEditCommentBinding
import com.example.diecasthangar.databinding.PopupEditDeleteBinding
import com.example.diecasthangar.databinding.RecyclerCommentRowLayoutBinding


class CommentRecyclerAdapter(
    private val onItemDeleted: (Comment) -> Unit,
    private val onItemEdited: (Comment, String) -> Unit,

) : RecyclerView.Adapter<CommentRecyclerAdapter.ViewHolder>(

) {
    var comments = ArrayList<Comment>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecyclerCommentRowLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = comments[position]

        holder.bodyTextView.text = comment.text
        holder.dateTextView.text = parseDate(comment.date)
        holder.usernameTextView.text = comment.username

        Glide.with(holder.itemView.context)
            .load(comment.avatarUri)
            .into(holder.avatarImageView)

        if (comment.user == getUser()!!.uid){
            holder.editDeletePopupBtn.visibility = View.VISIBLE
            holder.editDeletePopupBtn.setOnClickListener {
                val context = holder.itemView.context
                val inflater: LayoutInflater  =
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val binding = PopupEditDeleteBinding.inflate(inflater)
                val popup = PopupWindow(
                    binding.root,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
                // Closes the popup window when touch outside.
                popup.isOutsideTouchable = true
                popup.isFocusable = true
                // Removes default background.
                popup.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                popup.showAsDropDown(holder.editDeletePopupBtn, 0, 0)
                binding.postOptionsBtnDelete.setOnClickListener {
                    onItemDeleted(comment)
                    comments.removeAt(position)
                    notifyItemRemoved(position)
                    popup.dismiss()
                }
                binding.postOptionsBtnEdit.setOnClickListener {
                    popup.dismiss()
                    val inflaterEdit: LayoutInflater =
                        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                    val bindingEdit = PopupEditCommentBinding.inflate(inflaterEdit)
                    val popupEdit = PopupWindow(
                        bindingEdit.root,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT
                    )
                    // Closes the popup window when touch outside.
                    popupEdit.isOutsideTouchable = true
                    popupEdit.isFocusable = true
                    // Removes default background.
                    popupEdit.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    bindingEdit.editCommentEditText.setText(comment.text)

                    popupEdit.showAsDropDown(holder.itemView, Gravity.CENTER, 0, 0)
                    bindingEdit.editCommentEditButton.setOnClickListener {
                        val newText = bindingEdit.editCommentEditText.text.toString()
                        val newComment =
                            comment.copy(text = newText)
                        comments[position] = newComment
                        notifyItemChanged(position)
                        onItemEdited(comment, newText)
                        popupEdit.dismiss()
                    }
                }

            }
        }
    }

    inner class ViewHolder(binding: RecyclerCommentRowLayoutBinding): RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {
        val view = binding.root

        val avatarImageView = binding.postCommentAvatar
        val bodyTextView = binding.postCommentBody
        val dateTextView = binding.postCommentDate
        val usernameTextView = binding.postCommentUsername
        val editDeletePopupBtn = binding.commentBtnEditPopup


        init {
            view.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
        }
    }

    override fun getItemCount(): Int {
        return comments.size
    }
}