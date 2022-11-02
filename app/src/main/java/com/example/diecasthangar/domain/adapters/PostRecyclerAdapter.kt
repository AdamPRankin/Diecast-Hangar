package com.example.diecasthangar.domain.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.diecasthangar.R
import com.example.diecasthangar.core.inflate
import com.example.diecasthangar.data.Post
import com.example.diecasthangar.databinding.PopupAddReactionBinding
import com.example.diecasthangar.domain.remote.FirestoreRepository
import kotlinx.coroutines.*


class PostRecyclerAdapter: RecyclerView.Adapter<PostRecyclerAdapter.ViewHolder>() {
    var posts =  ArrayList<Post>()
    val firestoreRepository = FirestoreRepository()



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = parent.inflate(R.layout.recycler_post_row_layout, false)

        return ViewHolder(inflatedView)
    }

    @SuppressLint("InflateParams")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = posts[position]
        var currentImagePosition = 0
        holder.dateTextView.text = post.date.toString()
        holder.bodyTextView.text = post.text
        holder.userTextView.text = post.username


        val testPaint: Paint = Paint();
        testPaint.set(holder.bodyTextView.paint)
        var isMarquee = true
        val textWidth =  testPaint.measureText(holder.bodyTextView.text.toString())
        //TODO fix this check
        if (textWidth > 250){
            holder.showMoreButton.visibility = View.VISIBLE
            holder.showMoreButton.setOnClickListener{
                holder.bodyTextView.maxHeight = 9000
                holder.showMoreButton.visibility = View.GONE
            }
        }


        val avatarUri = post.avatar
        Glide.with(holder.itemView.context).load(avatarUri).into(holder.avatarImageView)

        if (post.images.isNotEmpty()){
            val firstImageUri: Uri = Uri.parse(post.images[0])
            Glide.with(holder.itemView.context).load(firstImageUri).into(holder.picImageview)
        }
        if (post.images.isEmpty()){
            holder.postImageHolder.visibility = View.GONE
        }
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
                val imageUri: Uri = Uri.parse(post.images[currentImagePosition])
                Glide.with(holder.itemView.context).load(imageUri).into(holder.picImageview)

            }
            holder.rightImageButton.setOnClickListener {
                if (currentImagePosition == post.images.size-1){
                    currentImagePosition = 0
                }
                else{
                    currentImagePosition +=1
                }
                val imageUri: Uri = Uri.parse(post.images[currentImagePosition])
                Glide.with(holder.itemView.context).load(imageUri).into(holder.picImageview)
            }
        }
        holder.reactButton.setOnClickListener {

            //TODO fix window not showing for last item in recycler
            val context = holder.itemView.context
            val inflater: LayoutInflater  =
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater;
            val binding = PopupAddReactionBinding.inflate(inflater)
            val popup = PopupWindow(
                binding.root,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            // Closes the popup window when touch outside.
            popup.isOutsideTouchable = true;
            popup.isFocusable = true;
            // Removes default background.
            popup.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


            binding.reactionPlane.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch{
                    firestoreRepository.addReaction("plane",post.id!!)
                }
                post.reactions["plane"] = post.reactions["plane"]!!.plus(1)
                notifyItemChanged(position)
                popup.dismiss()
            }
            binding.reactionLanding.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch{
                    firestoreRepository.addReaction("landing",post.id!!)
                }
                post.reactions["landing"] = post.reactions["landing"]!!.plus(1)
                notifyItemChanged(position)
                popup.dismiss()
            }
            binding.reactionTakeoff.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch{
                    firestoreRepository.addReaction("takeoff",post.id!!)
                }
                post.reactions["takeoff"] = post.reactions["takeoff"]!!.plus(1)
                notifyItemChanged(position)
                popup.dismiss()
            }


            popup.showAsDropDown(holder.reactButton, 0, 0)

            //check if the popup is below the screen, if so, adjust upwards
            val displayMetrics = context.resources.displayMetrics
            val height = displayMetrics.heightPixels

            val values = IntArray(2)
            holder.reactButton.getLocationOnScreen(values)
            val positionOfIcon = values[1]

            //TODO fix the offset by using actual height of window
            if (positionOfIcon >= (height - popup.height - holder.reactButton.height)) {
                val popupHeight = popup.height
                val yOffset =  -1*(popupHeight + holder.reactButton.height)
                popup.update(holder.reactButton, 0, yOffset, popup.width, popup.height)

            }

        }
        val reacts: Map<String,Int> = post.reactions
        val planeReacts = reacts["plane"]!!.toInt()
        val landingReacts = reacts["landing"]!!.toInt()
        val takeoffReacts = reacts["takeoff"]!!.toInt()
       //TODO logic to display top X reactions in initial snippet


        if ((planeReacts != null) && (planeReacts > 0) ) {
            holder.reactIcon1.setImageResource(R.drawable.ic_airplane_black_48dp)
            if (planeReacts > 1){
                holder.reactNumber1.text = planeReacts.toString()
            }
        }
        if ((landingReacts != null) && (landingReacts > 0) ) {
            holder.reactIcon2.setImageResource(R.drawable.ic_airplane_landing_black_48dp)
            if (landingReacts > 1){
                holder.reactNumber2.text = landingReacts.toString()
            }
        }
        if ((takeoffReacts != null) && (takeoffReacts > 0) ) {
            holder.reactIcon3.setImageResource(R.drawable.ic_airplane_takeoff_black_48dp)
            if (takeoffReacts > 1){
                holder.reactNumber3.text = takeoffReacts.toString()
            }
        }


    }

    inner class ViewHolder(v: View): RecyclerView.ViewHolder(v),
            View.OnClickListener {

        private var view: View = v
        val dateTextView: TextView = view.findViewById(R.id.post_date)
        val avatarImageView: ImageView = view.findViewById(R.id.post_avatar)
        val bodyTextView: TextView = view.findViewById(R.id.post_body)
        val userTextView: TextView = view.findViewById(R.id.post_username)
        val picImageview: ImageView = view.findViewById(R.id.post_picture)
        val postImageHolder: LinearLayout = view.findViewById(R.id.post_photo_container)
        val leftImageButton: Button = view.findViewById(R.id.btn_post_img_left)
        val rightImageButton: Button = view.findViewById(R.id.btn_post_img_right)
        val showMoreButton: Button = view.findViewById(R.id.post_btn_show_more)
        val commentButton: Button = view.findViewById(R.id.post_btn_comment)
        val reactButton: Button = view.findViewById(R.id.post_btn_react)
        val reactIcon1: ImageView = view.findViewById(R.id.post_reacts_1)
        val reactIcon2: ImageView = view.findViewById(R.id.post_reacts_2)
        val reactIcon3: ImageView = view.findViewById(R.id.post_reacts_3)
        val reactNumber1: TextView = view.findViewById(R.id.post_reaction1_number)
        val reactNumber2: TextView = view.findViewById(R.id.post_reaction2_number)
        val reactNumber3: TextView = view.findViewById(R.id.post_reaction3_number)

        init {
            v.setOnClickListener(this)
        }

        override fun onClick(v: View?) {

        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }



}