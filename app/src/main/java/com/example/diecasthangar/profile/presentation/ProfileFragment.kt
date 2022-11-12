package com.example.diecasthangar.profile.presentation

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.diecasthangar.R
import com.example.diecasthangar.UserViewModel
import com.example.diecasthangar.core.util.loadingDummyPost
import com.example.diecasthangar.databinding.FragmentProfileBinding
import com.example.diecasthangar.domain.adapters.PostRecyclerAdapter
import com.example.diecasthangar.domain.usecase.remote.getUser
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


open class ProfileFragment(uid: String = getUser()!!.uid): Fragment(), LifecycleOwner {
    private val profileUserId = uid
    private lateinit var binding: FragmentProfileBinding
    var imageAdded = false
    val viewModel: ProfileViewModel by viewModels { ProfileViewModel.Factory(profileUserId) }


    override fun onCreate(savedInstanceState: Bundle?) {
        val binding = FragmentProfileBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        // Inflate the layout for this fragment

        val profileImageView: ImageView = view.findViewById(R.id.profile_avatar)
        val profileEditAvatar: ImageView = view.findViewById(R.id.profile_edit_avatar)
        val profileUsername: TextView = view.findViewById(R.id.profile_name)
        val profileBioText: TextView = view.findViewById(R.id.profile_text_bio)
        val profileBioEditText: EditText = view.findViewById(R.id.profile_edit_text__bio)
        val profileEditButton: FloatingActionButton = view.findViewById(R.id.profile_btn_edit)
        val profileSaveButton: FloatingActionButton = view.findViewById(R.id.profile_btn_save)
        val profileLayout: MaterialCardView = view.findViewById(R.id.profile_layout)

        profileLayout.setOnClickListener{

        }

        val postRecyclerView = view.findViewById<RecyclerView>(R.id.profile_post_recycler)
        val postAdapter = PostRecyclerAdapter{
        }
        val postLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context)
        postRecyclerView.layoutManager = postLayoutManager
        postRecyclerView.adapter = postAdapter

        var loading = true
        val loadingPost = loadingDummyPost()
        postAdapter.posts.add(loadingPost)


        //val viewModel =  ViewModelProvider(this)[ProfileViewModel::class.java]
        viewModel.getPostMutableLiveData().observe(viewLifecycleOwner) { postList ->
            if (loading) {
                loading = false
                //remove loading Ui elements
                postAdapter.posts.removeAt(0)
                postAdapter.notifyItemRemoved(0)
            }
            // update UI
            postAdapter.notifyItemRemoved(0)
            val prevSize = postAdapter.posts.size
            postAdapter.posts = postList
            //X previous posts, so we want to update from index X onwards
            postAdapter.notifyItemRangeChanged(prevSize,postAdapter.itemCount)
        }

        viewModel.getUserBioMutableLiveData().observe(viewLifecycleOwner) { bio->
            profileBioText.text = bio
        }
        viewModel.getUsernameMutableLiveData().observe(viewLifecycleOwner) { name->
            profileUsername.text = name
        }
        viewModel.getAvatarMutableLiveData().observe(viewLifecycleOwner) { uri->
            //Glide.with(view).load(uri).into(profileEditAvatar)
            Glide.with(view).load(uri).into(profileImageView)


            if (imageAdded){
                Glide.with(view).load(uri).into(profileEditAvatar)
                //profileEditAvatar.setImageURI(Uri.parse(uri))
            }
        }

        postRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && dy > 0 && viewModel.postsLoading) {
                    viewModel.loadMorePosts()
                }
            }
        })
        val launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK
                && result.data != null
            ) {
                val imageUri = result.data!!.data
                viewModel.avatarUri.value = imageUri.toString()

                if (imageUri != null) {
                    val croppedImgFile = File(
                        requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        "croppedAvatar.jpg.jpg")

                    val options = UCrop.Options()

                    //TODO add dark theme support
                    val lightColor = ContextCompat.getColor(requireContext(),com.google.android.material.R.color.material_dynamic_tertiary60)
                    val darkColor = ContextCompat.getColor(requireContext(),com.google.android.material.R.color.material_dynamic_tertiary40)
                    options.setCropGridColor(lightColor)
                    options.setCropFrameColor(lightColor)
                    options.setToolbarColor(darkColor)
                    options.setActiveControlsWidgetColor(darkColor)
                    options.setLogoColor(darkColor)
                    options.setRootViewBackgroundColor(ContextCompat.getColor(requireContext(),com.google.android.material.R.color.material_dynamic_tertiary90))
                    options.setDimmedLayerColor(Color.TRANSPARENT)
                    options.setToolbarWidgetColor(ContextCompat.getColor(requireContext(),com.google.android.material.R.color.material_dynamic_tertiary60))


                    UCrop.of(imageUri,Uri.fromFile(croppedImgFile))
                        .withAspectRatio(1F, 1F)
                        .withMaxResultSize(200, 200)
                        .withOptions(options)
                        .start(requireContext(), this, UCrop.REQUEST_CROP)
                }
            }
        }
        if (profileUserId == getUser()!!.uid) {
            profileEditButton.visibility = View.VISIBLE
            profileEditButton.setOnClickListener {
                //launch editor
                profileBioEditText.visibility = View.VISIBLE
                profileBioEditText.setText(profileBioText.text)
                profileBioText.visibility = View.GONE
                profileEditButton.visibility = View.GONE
                profileSaveButton.visibility = View.VISIBLE
                profileEditAvatar.visibility = View.VISIBLE
                profileEditAvatar.setImageResource(R.drawable.image_edit)
                profileImageView.visibility = View.GONE


                profileEditAvatar.setOnClickListener {
                    val intent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    launcher.launch(intent)
                }

                profileSaveButton.setOnClickListener {
                    profileBioEditText.visibility = View.GONE
                    profileBioText.visibility = View.VISIBLE
                    profileEditAvatar.visibility = View.GONE
                    profileImageView.visibility = View.VISIBLE

                    //wait to display button to prevent spam toggle
                    lifecycleScope.launch {
                        delay(3000)
                        profileEditButton.visibility = View.VISIBLE
                    }
                    profileSaveButton.visibility = View.GONE

                    // prevent unneeded api call
                    if (profileBioText.text.toString() != profileBioEditText.text.toString()) {
                        profileBioText.text = profileBioEditText.text.toString()
                        viewModel.updateBio(profileBioEditText.text.toString())
                    }

                    //TODO upload pic and save to userdata
                    if (imageAdded) {
                        viewModel.updateAvatar()
                    }
                }
            }

        }
        return view
    }

    override fun onPause() {
        super.onPause()
        //TODO save profile data
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri: Uri? = data?.let { UCrop.getOutput(it) }
            viewModel.avatarUri.value = resultUri.toString()
            imageAdded = true

            val userViewModel: UserViewModel by activityViewModels()
            if (resultUri != null) {
                //update the avatar in dashboard so it will refresh
                userViewModel.setAvatarUri(resultUri)
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError: Throwable?  = data?.let { UCrop.getError(it) };
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}