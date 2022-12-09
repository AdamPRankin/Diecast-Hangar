package com.pingu.diecasthangar.ui.profile

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pingu.diecasthangar.R
import com.pingu.diecasthangar.data.model.Model
import com.pingu.diecasthangar.data.remote.getUser
import com.pingu.diecasthangar.databinding.PopupEditDeleteBinding
import com.pingu.diecasthangar.databinding.RecyclerModelRowLayoutBinding

class ModelRecyclerAdapter(
    private val onItemEdited: (Model, Int) -> Unit,
    private val onItemDeleted: (Model) -> Unit,
    private val onModelView: (Model) -> Unit,
): RecyclerView.Adapter<ModelRecyclerAdapter.ViewHolder>() {
    private var models = ArrayList<Model>()

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
        holder.modelTitle.text = "${model.scale} ${model.airline} ${model.frame} ${model.reg}"
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
            //show delete button only for model owner
            holder.modelEditDeleteButton.visibility = View.VISIBLE
            holder.modelEditDeleteButton.setOnClickListener {
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

                popup.showAsDropDown(holder.modelEditDeleteButton, 0, 0)

                //check if the popup is below the screen, if so, adjust upwards
                val displayMetrics = context.resources.displayMetrics
                val height = displayMetrics.heightPixels
                val popupHeightPx =
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 112f,
                        context.resources.displayMetrics)
                val values = IntArray(2)
                holder.modelEditDeleteButton.getLocationOnScreen(values)
                val positionOfIcon = values[1]
                if (positionOfIcon >= (height - popupHeightPx)) {
                    val yOffset = -1 * ( holder.modelEditDeleteButton.height + popupHeightPx.toInt())
                    popup.update(holder.modelEditDeleteButton, 0, yOffset, popup.width, popup.height)

                }

                binding.postOptionsBtnDelete.setOnClickListener {
                    onItemDeleted(model)
                    popup.dismiss()
                }
                binding.postOptionsBtnEdit.setOnClickListener {
                    onItemEdited(model, position)
                    popup.dismiss()
                }
            }
        }

        if (model.photos.size > 0) {
            holder.modelPhotoImageView.setOnClickListener {
                onModelView(model)
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
        val modelEditDeleteButton = binding.modelRowBtnEditPopup

        init {
            binding.root.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
        }
    }

    override fun getItemCount(): Int {
        return models.size
    }

    fun setData(newModel: List<Model>) {
        val diffCallback = ModelDiffCallback(models, newModel)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        models.clear()
        models.addAll(newModel)
        diffResult.dispatchUpdatesTo(this)
    }

    class ModelDiffCallback(oldList: List<Model>, newList: List<Model>) :
        DiffUtil.Callback() {
        private val oldModelList: List<Model>
        private val newModelList: List<Model>

        init {
            oldModelList = oldList
            newModelList = newList
        }

        override fun getOldListSize(): Int {
            return oldModelList.size
        }

        override fun getNewListSize(): Int {
            return newModelList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldModelList[oldItemPosition].id === newModelList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldPost: Model = oldModelList[oldItemPosition]
            val newPost: Model = newModelList[newItemPosition]
            return oldPost == newPost
        }

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            return super.getChangePayload(oldItemPosition, newItemPosition)
        }
    }



}