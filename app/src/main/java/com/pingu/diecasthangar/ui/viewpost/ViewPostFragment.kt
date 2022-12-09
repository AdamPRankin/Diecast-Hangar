package com.pingu.diecasthangar.ui.viewpost

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.PopupWindow
import androidx.core.view.iterator
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pingu.diecasthangar.R
import com.pingu.diecasthangar.core.util.parseDate
import com.pingu.diecasthangar.data.model.Post
import com.pingu.diecasthangar.databinding.FragmentViewPostBinding
import com.pingu.diecasthangar.databinding.PopupAddReactionBinding
import com.pingu.diecasthangar.ui.SideScrollImageRecyclerAdapter
import com.pingu.diecasthangar.ui.dashboard.DashboardViewModel
import kotlin.math.ceil


class ViewPostFragment(post: Post = Post()) : Fragment(), LifecycleOwner {
    private var _binding: FragmentViewPostBinding? = null
    private val binding get() = _binding!!
    private val currentPost = post


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewPostBinding.inflate(inflater, container, false)
        val view = binding.root

       val dashViewModel: DashboardViewModel by activityViewModels()

        val dateTextView = binding.viewPostDate
        val avatarImageView = binding.viewPostAvatar
        val bodyTextView = binding.viewPostBody
        val userTextView = binding.viewPostUsername
        val picImageview = binding.viewPostPicture
        val commentRecyclerView: RecyclerView = binding.viewPostCommentRecycler
        val photoContainer = binding.viewPostPhotoContainer

        val addCommentButton: Button = binding.viewPostCommentButton
        val commentEditText = binding.viewPostCommentEditText
        val photoRecyclerView = binding.viewPostPhotoRecycler
        val addReactButton = binding.viewPostBtnRect

        val showMoreButton = binding.postBtnShowMore
        val showLessButton = binding.postBtnShowLess


        val bounds = Rect()
        bodyTextView.paint.getTextBounds(
            bodyTextView.text.toString(), 0,
            bodyTextView.text.length, bounds
        )
        val textHeight = bounds.width()
        if (textHeight >= bodyTextView.maxHeight) {
            showMoreButton.visibility = View.VISIBLE
        } else {
            showMoreButton.visibility = View.GONE
        }
        //save max height
        val maxHeight = bodyTextView.maxHeight
        showMoreButton.setOnClickListener {
            bodyTextView.maxHeight = Int.MAX_VALUE
            showMoreButton.visibility = View.GONE
            showLessButton.visibility = View.VISIBLE
        }
        showLessButton.setOnClickListener {
            //restore original height
            bodyTextView.maxHeight = maxHeight
            showLessButton.visibility = View.GONE
            showMoreButton.visibility = View.VISIBLE
        }


        Glide.with(requireContext())
            .load(currentPost.avatar)
            .placeholder(R.drawable.ic_airplane_black_48dp)
            .into(avatarImageView)
        userTextView.text = currentPost.username
        dateTextView.text = parseDate(currentPost.date)
        bodyTextView.text = currentPost.text

        if (currentPost.images.size == 0) {
            photoRecyclerView.visibility = View.GONE
            picImageview.visibility = View.GONE
            photoContainer.visibility = View.GONE
        }
        if (currentPost.images.size == 1) {
            photoRecyclerView.visibility = View.GONE
            picImageview.visibility = View.VISIBLE
            Glide.with(requireContext())
                .load(currentPost.images[0].remoteUri)
                .placeholder(R.drawable.ic_airplane_black_48dp)
                .into(picImageview)
        } else {
            picImageview.visibility = View.GONE
            photoRecyclerView.visibility = View.VISIBLE
            val photoAdapter = SideScrollImageRecyclerAdapter({
                //deleting items disabled
            }, false)
            photoRecyclerView.adapter = photoAdapter
            photoRecyclerView.layoutManager =
                LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)

            photoAdapter.photos = currentPost.images
        }

        val commentAdapter = CommentRecyclerAdapter(
            // comment deleted
            { comment ->
                dashViewModel.deleteComment(comment.id)
            },
            //comment edited
            { comment, text ->
                dashViewModel.editComment(comment.id, text)
            }
        )
        val commentLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context)
        commentRecyclerView.layoutManager = commentLayoutManager
        commentRecyclerView.adapter = commentAdapter

        dashViewModel.loadComments(currentPost.id)
        dashViewModel.getCurrentCommentMutableLiveData().observe(viewLifecycleOwner) { comments ->
            commentAdapter.comments = comments
            commentAdapter.notifyDataSetChanged()
        }

        addCommentButton.setOnClickListener {

            if (commentEditText.text.toString().isNotEmpty()) {
                val commentText = commentEditText.text.toString()
                val pid = currentPost.id
                dashViewModel.addComment(pid, commentText)
            }
            parentFragmentManager.popBackStack()
        }

        addReactButton.setOnClickListener {

            val reactInflater: LayoutInflater =
                context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val reactPopupBinding = PopupAddReactionBinding.inflate(reactInflater)
            val popup = PopupWindow(
                reactPopupBinding.root,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            // Closes the popup window when touch outside.
            popup.isOutsideTouchable = true
            popup.isFocusable = true
            // Removes default background.
            popup.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            reactPopupBinding.reactionPlane.isFocusable = false
            reactPopupBinding.reactionTakeoff.isFocusable = false
            reactPopupBinding.reactionLanding.isFocusable = false

            for (item in reactPopupBinding.reactsLinearLayout) {
                item.setOnClickListener {
                    dashViewModel.addReact(item.contentDescription.toString(), currentPost.id)
                    popup.dismiss()
                }
            }
            //popup.showAtLocation(view, Gravity.CENTER, 0, 0)
            popup.showAsDropDown(addReactButton)

            //check if the popup is below the screen, if so, adjust upwards
            val displayMetrics = requireContext().resources.displayMetrics

            val screenWidth = displayMetrics.widthPixels

            //get padding and icon size in px
            val iconPx =
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 24f,
                    requireContext().resources.displayMetrics
                )
            val paddingPx =
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 8f,
                    requireContext().resources.displayMetrics
                )


            //calculate popup height
            val numIcons = binding.root.size
            val totalRowWidth = (iconPx * numIcons + paddingPx * (numIcons + 1))
            val numRows = ceil(totalRowWidth / screenWidth)
            val popupHeight = iconPx * numRows + paddingPx * (numIcons + 1)

            //add to button height and offset
            val buttonHeight = binding.viewPostBtnRect.height
            val yOffset = -1 * (popupHeight.toInt() + buttonHeight)
            popup.update(binding.viewPostBtnRect, 0, yOffset, popup.width, popup.height)


        }

        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}