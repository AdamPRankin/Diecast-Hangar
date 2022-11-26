package com.example.diecasthangar.ui

import android.R.attr.animationDuration
import android.R.id.toggle
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.CountDownTimer
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.core.view.iterator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.diecasthangar.R
import com.example.diecasthangar.core.util.getReactIcon
import com.example.diecasthangar.core.util.getTopReacts
import com.example.diecasthangar.core.util.parseDate
import com.example.diecasthangar.data.model.Post
import com.example.diecasthangar.data.model.Reaction
import com.example.diecasthangar.data.remote.FirestoreRepository
import com.example.diecasthangar.databinding.PopupAddReactionBinding
import com.example.diecasthangar.databinding.PopupEditPostBinding
import com.example.diecasthangar.databinding.PopupShowReactionsBinding
import com.example.diecasthangar.databinding.RecyclerPostRowLayoutBinding
import com.example.diecasthangar.domain.remote.getUser
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.*
import kotlin.math.hypot
import kotlin.math.roundToInt


class PostRecyclerAdapter(
    private val onAvatarClicked: (Post) -> Unit,
    private val onItemEdited: (Post) -> Unit,
    private val onItemDeleted: (Post) -> Unit,
    private val onCommentBtnClicked: (Post) -> Unit,
    private val onReactSelected: (Pair<String, String>) -> Unit
    ): RecyclerView.Adapter<PostRecyclerAdapter.ViewHolder>() {
    var posts =  ArrayList<Post>()
    private val firestoreRepository = FirestoreRepository()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecyclerPostRowLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("InflateParams", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]
        //addCommentsToPosts(firestoreRepository,10)
        var currentImagePosition = 0

        val displayDateString = parseDate(post.date)
        holder.dateTextView.text = displayDateString
        holder.bodyTextView.text = post.text
        holder.userTextView.text = post.username

        val testPaint = Paint()
        testPaint.set(holder.bodyTextView.paint)
        val textWidth =  testPaint.measureText(holder.bodyTextView.text.toString())
        val text = holder.bodyTextView.text.toString()
        val textBounds = Rect()
        //mTextPaint.getTextBounds(mText, 0, mText.length, textBounds)
        val mTextWidth = textBounds.width()
        val textHeight = textBounds.height()
        //TODO fix this check

        if (textWidth > 250){
            holder.showMoreButton.visibility = View.VISIBLE
            holder.showMoreButton.setOnClickListener{
                holder.bodyTextView.maxHeight = 9000
                holder.showMoreButton.visibility = View.GONE
            }
        }
        else{
            holder.showMoreButton.visibility = View.GONE
        }

        val avatarUri = post.avatar
        Glide.with(holder.itemView.context).load(avatarUri).into(holder.avatarImageView)

        holder.avatarImageView.setOnClickListener {
            onAvatarClicked(post)
        }

        if (post.images.isNotEmpty()){
            val firstImageUri: Uri = Uri.parse(post.images[0].remoteUri)
            Glide.with(holder.itemView.context).load(firstImageUri).into(holder.picImageview)
        }
        if (post.images.isEmpty()){
            holder.postImageHolder.visibility = View.GONE
        }
        else {
            //set visible explicitly to avoid recycle row reuse bugs when scrolling back up
            holder.postImageHolder.visibility = View.VISIBLE
        }
        //remove scroll buttons if only one image
        if (post.images.size < 2) {
            holder.leftImageButton.visibility = View.GONE
            holder.rightImageButton.visibility = View.GONE
        }
        else {
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

           for (item in binding.reactsLinearLayout) {
               item.setOnClickListener {
                   onReactSelected(Pair(item.contentDescription.toString(), post.id))
                   popup.dismiss()
               }
           }

/*            popup.setOnDismissListener {
                object : CountDownTimer(animationDuration.toLong(), 100) {
                    override fun onTick(millisUntilFinished: Long) {}
                    override fun onFinish() {
                        popup.dismiss()
                    }
                }.start()
            }*/

/*            for (item in binding.reactsLinearLayout) {
                item.setOnClickListener {  clickedButton ->
                    onReactSelected(Pair(item.contentDescription.toString(),post.id))
                    for (item in binding.reactsLinearLayout) {
                        if (item.id != clickedButton.id){
                            // get the center for the clipping circle
                            item.visibility = View.GONE
                            val k = IntArray(2)
                            val h = clickedButton.getLocationInWindow(k)
                            popup.update(h,popup.width,popup.height)
                            popup.update()
                            //popup.update(48,48)
                            //popup.dismiss()
*//*                            val cx = item.width / 2
                            val cy = item.height / 2

                            // get the final radius for the clipping circle
                            val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()

                            // create the animator for this view (the start radius is zero)
                            val anim = ViewAnimationUtils.createCircularReveal(item, cx, cy, 0f, finalRadius)

                            anim.addListener(object : AnimatorListenerAdapter() {

                                override fun onAnimationEnd(animation: Animator) {
                                    super.onAnimationEnd(animation)
                                    item.visibility = View.INVISIBLE
                                    popup.dismiss()
                                }
                            })
                            anim.start()*//*
                        }
                    }
                    popup.update()


                }
            }*/


            popup.showAsDropDown(holder.reactButton)
            popup.animationStyle = com.google.android.material.R.style.Animation_AppCompat_DropDownUp


            //check if the popup is below the screen, if so, adjust upwards
            val displayMetrics = context.resources.displayMetrics
            val height = displayMetrics.heightPixels

            val values = IntArray(2)
            holder.reactButton.getLocationOnScreen(values)
            val positionOfIcon = values[1]

            // adjust for window padding
            val px =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32f,
                    context.resources.displayMetrics).roundToInt()

            if (positionOfIcon >= (height - holder.reactButton.height)) {
                val yOffset = -1 * ( holder.reactButton.height + px)
                popup.update(holder.reactButton, 0, yOffset, popup.width, popup.height)
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

/*        if (firstReactNumber > 0 ) {

            if (firstReactNumber > 1){
                holder.reactNumber1.text = secondReactNumber.toString()
            }
        }
        else {
            holder.reactNumber1.text = ""
        }
        if (secondReactNumber > 0) {

            if (secondReactNumber > 1){
                holder.reactNumber1.text = ""
            }
        }
        else {
            holder.reactNumber2.text = ""
        }
        if (thirdReactNumber > 0) {

            if (thirdReactNumber > 1){
                holder.reactNumber3.text = thirdReactNumber.toString()
            }
        }
        else {
            holder.reactNumber3.text = ""
        }*/

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

                //check if the popup is below the screen, if so, adjust upwards
                val displayMetrics = context.resources.displayMetrics
                val height = displayMetrics.heightPixels

                val values = IntArray(2)
                holder.reactButton.getLocationOnScreen(values)
                val positionOfIcon = values[1]

                // adjust for window padding
                val px =
                    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32f,
                        context.resources.displayMetrics).roundToInt()

                if (positionOfIcon >= (height - holder.reactButton.height)) {
                    val yOffset = -1 * ( holder.reactButton.height + px)
                    popup.update(holder.reactButton, 0, yOffset, popup.width, popup.height)
                }
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
                val binding = PopupEditPostBinding.inflate(inflater)
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
            holder.showMoreButton.visibility = View.GONE
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
        val commentButton: Button = binding.postBtnComment
        val reactButton: Button = binding.postBtnReact
        val editPostPopupButton: FloatingActionButton = binding.postBtnEditPopup
        val reactIcon1: ImageView = binding.postReacts1
        val reactIcon2: ImageView = binding.postReacts2
        val reactIcon3: ImageView = binding.postReacts3
/*        val reactNumber1: TextView = binding.postReaction1Number
        val reactNumber2: TextView = binding.postReaction2Number
        val reactNumber3: TextView = binding.postReaction3Number*/
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
}