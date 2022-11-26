package com.example.diecasthangar.ui.profile

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.TypedValue
import android.view.*
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.diecasthangar.R
import com.example.diecasthangar.data.model.Model
import com.example.diecasthangar.data.model.Post
import com.example.diecasthangar.databinding.PopupEditPostBinding
import com.example.diecasthangar.databinding.PopupViewModelPhotosBinding
import com.example.diecasthangar.databinding.RecyclerModelRowLayoutBinding
import com.example.diecasthangar.domain.remote.getUser
import com.example.diecasthangar.ui.SideScrollImageRecyclerAdapter
import kotlin.math.roundToInt

class ModelRecyclerAdapter(
    private val onItemEdited: (Model, Int) -> Unit,
    private val onItemDeleted: (Model) -> Unit,
): RecyclerView.Adapter<ModelRecyclerAdapter.ViewHolder>() {
    var models = ArrayList<Model>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowBinding = RecyclerModelRowLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(rowBinding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = models[position]

        if (model.photos.size > 0) {
            Glide.with(holder.itemView.context).load(model.photos[0].remoteUri)
                .placeholder(R.drawable.airplane)
                .into(holder.modelPhotoImageView)
        }
        else {
            holder.modelPhotoImageView.visibility = View.GONE
        }

        holder.modelComment.text = model.comment
        holder.modelTitle.text = "${model.scale} ${model.airline} ${model.frame}"
        holder.modelLivery.text = model.livery
        holder.modelBrand.text = model.manufacturer

        when (model.manufacturer) {
            "NG models" -> holder.modelBrandIcon.setImageResource(R.drawable.ng)
            "JC Wings" -> holder.modelBrandIcon.setImageResource(R.drawable.jc)
            "Phoenix" -> holder.modelBrandIcon.setImageResource(R.drawable.phoenix)
            "Panda" -> holder.modelBrandIcon.setImageResource(R.drawable.panda)
            "Herpa" -> holder.modelBrandIcon.setImageResource(R.drawable.herpa)
            "Inflight200" -> holder.modelBrandIcon.setImageResource(R.drawable.if200)
            "Gemini Jets" -> holder.modelBrandIcon.setImageResource(R.drawable.gj)
            else -> {
                holder.modelBrandIcon.visibility = View.GONE
            }
        }

        if (model.userID == getUser()?.uid) {
            holder.modelEditPopup.visibility = View.VISIBLE
            holder.modelEditPopup.setOnClickListener {
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

                popup.showAsDropDown(holder.modelEditPopup, 0, 0)

                //TODO make sure displays onscreen
                //check if the popup is below the screen, if so, adjust upwards

                binding.postOptionsBtnDelete.setOnClickListener {
                    onItemDeleted(model)
                    models.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position,models.size)
                    popup.dismiss()
                }
                binding.postOptionsBtnEdit.setOnClickListener {
                    onItemEdited(model, position)
                    popup.dismiss()
                }
            }
        }

        if (model.photos.size > 1) {
            holder.modelPhotoImageView.setOnClickListener {
                val context = holder.itemView.context
                val inflater: LayoutInflater  =
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val binding = PopupViewModelPhotosBinding.inflate(inflater)
                val popup = PopupWindow(
                    binding.root,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT

                )
                popup.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                val photoRecyclerView = binding.modelPopupRecyclerview
                val photoAdapter = SideScrollImageRecyclerAdapter({ _ ->
                    //display only mode
                },false)
                photoRecyclerView.adapter = photoAdapter
                photoRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context,
                    LinearLayoutManager.HORIZONTAL, false)
                photoAdapter.photos = model.photos
                popup.showAtLocation(holder.itemView, Gravity.CENTER, 0, 0)



                binding.modelPopupExit.setOnClickListener {
                    popup.dismiss()
                }
            }
        }
    }

    inner class ViewHolder(binding: RecyclerModelRowLayoutBinding): RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        val modelComment = binding.modelRowCommentTextview
        val modelBrand = binding.modelRowBrandTextview
        val modelLivery = binding.modelRowLiveryTextview
        val modelTitle = binding.modelRowTitleTextview

        val modelPhotoImageView = binding.modelRowImageView
        val modelBrandIcon = binding.modelRowBrandIcon
        val modelEditPopup = binding.modelRowBtnEditPopup


        init {
            binding.root.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
        }
    }

    override fun getItemCount(): Int {
        return models.size
    }



}