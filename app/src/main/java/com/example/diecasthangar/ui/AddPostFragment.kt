package com.example.diecasthangar.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diecasthangar.R
import com.example.diecasthangar.data.model.Photo
import com.example.diecasthangar.data.model.Post
import com.example.diecasthangar.databinding.FragmentAddPostBinding
import com.example.diecasthangar.data.remote.getUser
import com.example.diecasthangar.ui.dashboard.DashboardViewModel
import com.example.diecasthangar.ui.viewpost.AddPostViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import me.shouheng.compress.Compress
import me.shouheng.compress.concrete
import me.shouheng.compress.strategy.config.ScaleMode


class AddPostFragment(post: Post? = null, editMode: Boolean = false) : Fragment() {
    private val currentPost = post
    private val editing = editMode

    private var _binding: FragmentAddPostBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPostBinding.inflate(inflater, container, false)
        val view = binding.root

        if (currentPost == null && editing){
            class CustomException (message: String) : Exception(message)
            throw CustomException ("do not use editing flag with null post")
        }
        val viewModel: AddPostViewModel by viewModels {
            AddPostViewModel.Factory(
                currentPost,
                editing
            )
        }
        val userViewModel: UserViewModel by activityViewModels()
        val dashboardViewModel: DashboardViewModel by activityViewModels()
        val avatarUri = userViewModel.getAvatarUri().value
        val username = userViewModel.getUsername()
        var photos: ArrayList<Photo> = ArrayList()
        val localUris: ArrayList<Uri> = ArrayList()

        val addPhotoButton: FloatingActionButton = view.findViewById(R.id.add_post_btn_add_images)
        val confirmButton: Button = view.findViewById(R.id.add_post_btn_add)
        if (editing){
            confirmButton.text = resources.getText(R.string.edit)
        }

        val cancelButton: Button = view.findViewById(R.id.add_post_btn_cancel)
        val postBodyEditText: EditText = view.findViewById(R.id.add_post_text_field)
        val addImageRecyclerView: RecyclerView = view.findViewById(R.id.add_post_img_recycler)
        val addPostProgressBar: ProgressBar = view.findViewById(R.id.add_post_progress_bar)
        val addImageLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(
            view.context,LinearLayoutManager.HORIZONTAL, false)
        val addImageAdapter = SideScrollImageRecyclerAdapter(
            // photo deleted
            { photo ->
            photos.remove(photo)
        },canDeleteItems = true)
        addImageRecyclerView.layoutManager = addImageLayoutManager
        addImageRecyclerView.adapter = addImageAdapter

        if (editing){
            photos.addAll(currentPost!!.images)
            addImageAdapter.photos = photos
            addImageAdapter.notifyDataSetChanged()
        }

        viewModel.getPostBodyMutableLiveData().observe(viewLifecycleOwner) { text->
            postBodyEditText.setText(text)
        }
        viewModel.getPhotoMutableLiveData().observe(viewLifecycleOwner) {
            //photos = it
            addImageAdapter.photos = it
            addImageAdapter.notifyDataSetChanged()
        }

        val launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK
                && result.data != null
            ) {
                if (result.data!!.clipData != null) {
                    // reset list
                    photos = arrayListOf()
                    val mClipData = result.data!!.clipData
                    for (i in 0 until mClipData!!.itemCount) {
                        val item = mClipData.getItemAt(i)
                        val imageUri = item.uri
                        val compressedFile = Compress.with(requireContext(), imageUri)
                            .setQuality(80)
                            .concrete {
                                withMaxWidth(300f)
                                withMaxHeight(300f)
                                withScaleMode(ScaleMode.SCALE_HEIGHT)
                                withIgnoreIfSmaller(true)
                            }.get()
                        val compressedUri = Uri.fromFile(compressedFile)
                        val photo = Photo(remoteUri = "", localUri = compressedUri)
                        photos.add(photo)
                        localUris.add(compressedUri)
                    }
                } else if (result.data!!.data != null) {
                    val imageUri = result.data!!.data
                    val compressedFile = Compress.with(requireContext(), imageUri!!)
                        .setQuality(80)
                        .concrete {
                            withMaxWidth(300f)
                            withMaxHeight(300f)
                            withScaleMode(ScaleMode.SCALE_HEIGHT)
                            withIgnoreIfSmaller(true)
                        }.get()
                    val compressedUri = Uri.fromFile(compressedFile)
                    val photo = Photo(localUri = compressedUri)
                    photos.add(photo)
                    localUris.add(compressedUri)
                }
                viewModel.addPhotos(photos)
                addImageAdapter.photos = viewModel.getPhotoMutableLiveData().value!!
                addImageAdapter.notifyDataSetChanged()
            }
        }
        addPhotoButton.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            launcher.launch(intent)
        }

        confirmButton.setOnClickListener {
            if (editing){
                viewModel.updatePostBodyText(postBodyEditText.text.toString())
                viewModel.updatePostPhotos()
                val newPost = Post(text = postBodyEditText.text.toString(), images = photos, avatar = avatarUri!!,
                    username = username, user = getUser()!!.uid)
                dashboardViewModel.itemEdited(Pair(currentPost!!,newPost))
                parentFragmentManager.popBackStack()
            }
            else {
                addPostProgressBar.visibility = View.VISIBLE
                val text = postBodyEditText.text.toString()
                val photos = addImageAdapter.photos

                val newPost = Post(text = text, images = photos, avatar = avatarUri!!,
                    username = username, user = getUser()!!.uid)
                dashboardViewModel.addPost(newPost)
                parentFragmentManager.popBackStack()
            }
        }
        cancelButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
