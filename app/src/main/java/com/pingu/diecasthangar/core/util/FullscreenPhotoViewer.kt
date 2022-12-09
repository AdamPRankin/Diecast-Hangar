package com.pingu.diecasthangar.core.util

import android.content.Context
import android.content.res.Configuration
import android.view.*
import android.widget.PopupWindow
import androidx.recyclerview.widget.LinearLayoutManager
import com.pingu.diecasthangar.data.model.Photo
import com.pingu.diecasthangar.databinding.PopupViewModelPhotosBinding
import com.pingu.diecasthangar.ui.FullScreenImageRecyclerAdapter
import com.pingu.diecasthangar.ui.SideScrollImageRecyclerAdapter


class FullscreenPhotoViewer(
    private val onPopupStart: (ArrayList<Photo>) -> Unit,
    private val onPopupEnd: () -> Unit,
) {
    fun showPhotoPopup(
        photos: ArrayList<Photo>, orientation: Int, view: View) {
        onPopupStart(photos)
        //dashViewModel.currentPostPhotosViewing = post
        val height =
            when (orientation) {
                Configuration.ORIENTATION_PORTRAIT -> {
                    ViewGroup.LayoutParams.WRAP_CONTENT
                }
                Configuration.ORIENTATION_LANDSCAPE -> {
                    ViewGroup.LayoutParams.MATCH_PARENT
                }
                else -> {
                    ViewGroup.LayoutParams.WRAP_CONTENT
                }
            }
        val context = view.context
        val popupInflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupBinding = PopupViewModelPhotosBinding.inflate(popupInflater)
        val popup = PopupWindow(
            popupBinding.root,
            WindowManager.LayoutParams.MATCH_PARENT,
            height

        )

        val photoRecyclerView = popupBinding.modelPopupRecyclerview
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            val photoAdapter = SideScrollImageRecyclerAdapter({
                //display only mode
            }, false)
            photoRecyclerView.adapter = photoAdapter
            photoRecyclerView.layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL, false
            )
            photoAdapter.photos = photos
        } else {
            val photoAdapter = FullScreenImageRecyclerAdapter()
            photoRecyclerView.adapter = photoAdapter
            photoRecyclerView.layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL, false
            )
            photoAdapter.photos = photos

        }
        popupBinding.modelPopupExit.setOnClickListener {
            //dashViewModel.currentPostPhotosViewing = null
            onPopupEnd()
            popup.dismiss()
        }
        popup.showAtLocation(view, Gravity.CENTER, 0, 0)
    }
}