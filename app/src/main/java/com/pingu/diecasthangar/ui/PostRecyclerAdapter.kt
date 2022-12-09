package com.pingu.diecasthangar.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.core.view.iterator
import androidx.core.view.size
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pingu.diecasthangar.core.util.getReactIcon
import com.pingu.diecasthangar.core.util.getTopReacts
import com.pingu.diecasthangar.core.util.loadingDummyPost
import com.pingu.diecasthangar.core.util.parseDate
import com.pingu.diecasthangar.data.model.Post
import com.pingu.diecasthangar.data.model.Reaction
import com.pingu.diecasthangar.data.remote.getUser
import com.pingu.diecasthangar.databinding.PopupAddReactionBinding
import com.pingu.diecasthangar.databinding.PopupEditDeleteBinding
import com.pingu.diecasthangar.databinding.PopupShowReactionsBinding
import com.pingu.diecasthangar.databinding.RecyclerPostRowLayoutBinding
import kotlin.math.ceil


class PostRecyclerAdapter(
    private val onAvatarClicked: (Post) -> Unit,
    private val onItemEdited: (Post) -> Unit,
    private val onItemDeleted: (Post) -> Unit,
    private val onCommentBtnClicked: (Post) -> Unit,
    private val onReactSelected: (Pair<String, String>) -> Unit,
    private val onPhotoClicked: (Post) -> Unit,
    ): RecyclerView.Adapter<PostRecyclerAdapter.ViewHolder>() {
    var posts =  arrayListOf(loadingDummyPost())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecyclerPostRowLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("InflateParams", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]
        var currentImagePosition = 0

        val displayDateString = parseDate(post.date)
        holder.dateTextView.text = displayDateString
        holder.bodyTextView.text = post.text
        holder.userTextView.text = post.username

        val bounds = Rect()
        val bodyText = holder.bodyTextView
        bodyText.paint.getTextBounds(bodyText.text.toString(), 0, bodyText.text.length, bounds)
        val textHeight = bounds.width()
        if (textHeight >= bodyText.maxHeight){
            holder.showMoreButton.visibility = View.VISIBLE
        }
        else{
            holder.showMoreButton.visibility = View.GONE
        }
        //save max height
        val maxHeight = bodyText.maxHeight
        holder.showMoreButton.setOnClickListener{
            holder.bodyTextView.maxHeight = Int.MAX_VALUE
            holder.showMoreButton.visibility = View.GONE
            holder.showLessButton.visibility = View.VISIBLE
        }
        holder.showLessButton.setOnClickListener {
            //restore original height
            holder.bodyTextView.maxHeight = maxHeight
            holder.showLessButton.visibility = View.GONE
            holder.showMoreButton.visibility = View.VISIBLE
        }

        val avatarUri = post.avatar
        Glide.with(holder.itemView.context).load(avatarUri).into(holder.avatarImageView)

        holder.avatarImageView.setOnClickListener {
            onAvatarClicked(post)
        }

        if (post.images.isNotEmpty()){
            holder.postImageHolder.visibility = View.VISIBLE
            val firstImageUri: Uri = Uri.parse(post.images[0].remoteUri)
            Glide.with(holder.itemView.context).load(firstImageUri).into(holder.picImageview)
        }
        else if (post.images.isEmpty()){
            holder.postImageHolder.visibility = View.GONE
        }

        //remove scroll buttons if only one image
        if (post.images.size < 2) {
            holder.leftImageButton.visibility = View.GONE
            holder.rightImageButton.visibility = View.GONE
        }
        else {
            holder.leftImageButton.visibility = View.VISIBLE
            holder.rightImageButton.visibility = View.VISIBLE
            holder.leftImageButton.setOnClickListener {
                if (currentImagePosition == 0){
                    currentImagePosition = post.images.size-1
                }
                else{
                    currentImagePosition -=1
                }
                val imageUri: Uri = Uri.parse(post.images[currentImagePosition].remoteUri)
                Glide.with(holder.itemView.context).load(imageUri).into(holder.picImageview)

            }
            holder.rightImageButton.setOnClickListener {
                if (currentImagePosition == post.images.size-1){
                    currentImagePosition = 0
                }
                else{
                    currentImagePosition +=1
                }
                val imageUri: Uri = Uri.parse(post.images[currentImagePosition].remoteUri)
                Glide.with(holder.itemView.context).load(imageUri).into(holder.picImageview)
            }
        }

        holder.picImageview.setOnClickListener {
            onPhotoClicked(post)
        }

        //popup to display reaction add menu
        holder.reactButton.setOnClickListener {
            val context = holder.itemView.context
            val inflater: LayoutInflater  =
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val binding = PopupAddReactionBinding.inflate(inflater)
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

            binding.reactionPlane.isFocusable = false
            binding.reactionTakeoff.isFocusable = false
            binding.reactionLanding.isFocusable = false

            //set listener for each con in the layout
           for (item in binding.reactsLinearLayout) {
               item.setOnClickListener {
                   //todo use another method to get name
                   onReactSelected(Pair(item.contentDescription.toString(), post.id))
                   popup.dismiss()
               }
           }

            popup.showAsDropDown(holder.reactButton)
            //check if the popup is below the screen, if so, adjust upwards

            //use application context to get resources
            val displayMetrics = context.applicationContext.resources.displayMetrics
            val height = displayMetrics.heightPixels

            val values = IntArray(2)
            holder.reactButton.getLocationOnScreen(values)
            val positionOfIcon = values[1]

            val screenWidth = displayMetrics.widthPixels
            val iconPx =
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 24f,
                    context.resources.displayMetrics)
            val paddingPx =
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 8f,
                    context.resources.displayMetrics)
            val numIcons = binding.reactsLinearLayout.size
            val totalRowWidth = (iconPx * numIcons + paddingPx * (numIcons+1))
            val numRows = ceil(totalRowWidth/screenWidth)
            val popupHeight = iconPx * numRows + paddingPx * (numIcons+1)
            val buttonHeight = holder.reactButton.height

            // adjust for window padding
            if (positionOfIcon >= (height - buttonHeight)) {
                val yOffset = -1 * (buttonHeight + popupHeight.toInt())
                popup.update(holder.reactButton, 0, yOffset, screenWidth, popup.height)
            }
        }

        holder.commentButton.setOnClickListener {
            onCommentBtnClicked(post)
        }
        val reacts: Map<String,Int> = post.reactions

        val topThreeReacts = getTopReacts(reacts,3)

        val (firstReactType, firstReactNumber) = topThreeReacts[0]
        val (secondReactType, secondReactNumber) = topThreeReacts[1]
        val (thirdReactType, thirdReactNumber ) = topThreeReacts[2]

        //set react icons, sets to blank if fewer than 3 exist
        holder.reactIcon1.setImageResource(getReactIcon(firstReactType))
        holder.reactIcon2.setImageResource(getReactIcon(secondReactType))
        holder.reactIcon3.setImageResource(getReactIcon(thirdReactType))

        for (item in holder.reactsLayout){
            item.setOnClickListener {
                val context = holder.itemView.context
                val inflater: LayoutInflater  =
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val binding = PopupShowReactionsBinding.inflate(inflater)
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

                //get list of reactions and map to model class
                val reactions = post.reactions.map { Reaction(it.key, getReactIcon(it.key),it.value) }

                val reactsRecycler = binding.reactsRecyclerview
                val reactsAdapter = ReactionsRecyclerAdapter()
                val reactsLayoutManager = LinearLayoutManager(context)
                reactsRecycler.adapter = reactsAdapter
                reactsRecycler.layoutManager = reactsLayoutManager
                reactsAdapter.reacts = reactions as ArrayList<Reaction>
                reactsAdapter.notifyDataSetChanged()

                popup.showAtLocation(holder.itemView, Gravity.CENTER, 0, 0)
            }
        }

        //show edit/delete button on current user posts
        if (post.user == getUser()?.uid) {
            holder.editPostPopupButton.visibility = View.VISIBLE
            holder.editPostPopupButton.setOnClickListener {
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

                popup.showAsDropDown(holder.editPostPopupButton, 0, 0)
                binding.postOptionsBtnDelete.setOnClickListener {
                    onItemDeleted(post)
                    posts.removeAt(position)
                    notifyItemRemoved(position)
                    popup.dismiss()
                }
                binding.postOptionsBtnEdit.setOnClickListener {
                    onItemEdited(post)
                    popup.dismiss()
                }

            }
        }
        else {
            holder.editPostPopupButton.visibility = View.GONE
        }

        //disable buttons if post is dummy
        if (post.id == "123"){
            holder.commentButton.visibility = View.GONE
            holder.reactButton.visibility = View.GONE
        }
        else{
            holder.commentButton.visibility = View.VISIBLE
            holder.reactButton.visibility = View.VISIBLE
        }
    }

    inner class ViewHolder(binding: RecyclerPostRowLayoutBinding): RecyclerView.ViewHolder(binding.root),
            View.OnClickListener {

        private var view: View = binding.root

        val dateTextView: TextView = binding.postDate
        val avatarImageView: ImageView = binding.postAvatar
        val bodyTextView: TextView = binding.postBody
        val userTextView: TextView = binding.postUsername
        val picImageview: ImageView = binding.postPicture
        val postImageHolder: LinearLayout = binding.postPhotoContainer
        val leftImageButton: Button = binding.btnPostImgLeft
        val rightImageButton: Button = binding.btnPostImgRight
        val showMoreButton: Button = binding.postBtnShowMore
        val showLessButton: Button = binding.postBtnShowLess
        val commentButton: Button = binding.postBtnComment
        val reactButton: Button = binding.postBtnReact
        val editPostPopupButton: ImageButton = binding.postBtnEditPopup
        val reactIcon1: ImageView = binding.postReacts1
        val reactIcon2: ImageView = binding.postReacts2
        val reactIcon3: ImageView = binding.postReacts3
        val reactsLayout = binding.postReactionsLayout

        init {
            view.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    fun setData(newPost: List<Post>) {
        val diffCallback = PostDiffCallback(posts, newPost)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        posts.clear()
        posts.addAll(newPost)
        diffResult.dispatchUpdatesTo(this)
    }

    class PostDiffCallback(oldList: List<Post>, newList: List<Post>) :
        DiffUtil.Callback() {
        private val oldPostList: List<Post>
        private val newPostList: List<Post>

        init {
            oldPostList = oldList
            newPostList = newList
        }

        override fun getOldListSize(): Int {
            return oldPostList.size
        }

        override fun getNewListSize(): Int {
            return newPostList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldPostList[oldItemPosition].id === newPostList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldPost: Post = oldPostList[oldItemPosition]
            val newPost: Post = newPostList[newItemPosition]
            return oldPost.text == newPost.text && oldPost.images == newPost.images
        }

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            return super.getChangePayload(oldItemPosition, newItemPosition)
        }
    }
}