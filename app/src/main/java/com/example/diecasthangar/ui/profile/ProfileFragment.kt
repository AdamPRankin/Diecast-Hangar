package com.example.diecasthangar.ui.profile

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
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
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.diecasthangar.R
import com.example.diecasthangar.data.model.Model
import com.example.diecasthangar.data.model.Photo
import com.example.diecasthangar.data.remote.Response
import com.example.diecasthangar.data.remote.getUser
import com.example.diecasthangar.databinding.FragmentProfileBinding
import com.example.diecasthangar.databinding.PopupAddFriendBinding
import com.example.diecasthangar.databinding.PopupAddModelBinding
import com.example.diecasthangar.ui.*
import com.example.diecasthangar.ui.viewpost.ViewPostFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.shouheng.compress.Compress
import me.shouheng.compress.concrete
import me.shouheng.compress.strategy.config.ScaleMode
import java.io.File


open class ProfileFragment(uid: String = getUser()!!.uid): Fragment(), LifecycleOwner {
    private val profileUserId = uid
    private var imageAdded = false
    private val viewModel: ProfileViewModel by viewModels { ProfileViewModel.Factory(profileUserId) }

    private var _binding: FragmentProfileBinding? = null
    // This property is only valid between onCreateView and
   // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root
        // Inflate the layout for this fragment

        val profileImageView: ImageView = binding.profileAvatar
        val profileEditAvatar: ImageView = binding.profileEditAvatar
        val profileUsername: TextView = binding.profileName
        val profileBioText: TextView = binding.profileTextBio
        val profileBioEditText: EditText = binding.profileEditTextBio
        val profileEditButton: FloatingActionButton = binding.profileBtnEdit
        val profileSaveButton: FloatingActionButton = binding.profileBtnSave
        val profileTogglePosts: Button = binding.profileBtnPosts
        val profileToggleModels: Button = binding.profileBtnModels
        val profileToggleFriends: Button = binding.profileBtnFriends
        val profileAddModelButton: FloatingActionButton = binding.profileBtnAddModel
        val profileAddFriendButton: FloatingActionButton = binding.profileBtnAddFriend

        val photos = ArrayList<Photo>()
        val modelPhotoLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK
                && result.data != null
            ) {
                if (result.data!!.clipData != null) {
                    // reset list
                    val mClipData = result.data!!.clipData
                    lifecycleScope.launch {
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
                            val photo = Photo(localUri = compressedUri, remoteUri = "")
                            photos.add(photo)
                            viewModel.addCurrentModelPhotos(arrayListOf(photo))
                        }
                    }
                } else if (result.data!!.data != null) {
                    val imageUri = result.data!!.data!!
                    val compressedFile = Compress.with(requireContext(), imageUri)
                        .setQuality(80)
                        .concrete {
                            withMaxWidth(300f)
                            withMaxHeight(300f)
                            withScaleMode(ScaleMode.SCALE_HEIGHT)
                            withIgnoreIfSmaller(true)
                        }.get()
                    val compressedUri = Uri.fromFile(compressedFile)
                    val photo = Photo(localUri = compressedUri, remoteUri = "")
                    photos.add(photo)
                    viewModel.addCurrentModelPhotos(arrayListOf(photo))
                }
            }
        }

        fun editModel(model: Model,editing: Boolean = true) {
            val context = view.context
            viewModel.clearCurrentModelPhotos()
            if (editing) {
                viewModel.addCurrentModelPhotos(model.photos)
            }

            val addModelPopupInflater: LayoutInflater  =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val binding = PopupAddModelBinding.inflate(addModelPopupInflater)
            val popup = PopupWindow(
                binding.root,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            // Closes the popup window when touch outside.
            popup.isOutsideTouchable = true
            popup.isFocusable = true

            // Removes default background.
            popup.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // brand autocomplete
            val autoCompleteBrand = binding.addModelAutocompleteBrand
            val brands: Array<out String> = resources.getStringArray(R.array.brands_array)
            autoCompleteBrand.setAdapter(ArrayAdapter(requireContext(),
                android.R.layout.simple_list_item_1, brands).also { adapter ->
                autoCompleteBrand.setAdapter(adapter)})

            // scale autocomplete
            val autoCompleteScale = binding.addModelAutocompleteScale
            val scales: Array<out String> = resources.getStringArray(R.array.scales_array)
            autoCompleteScale .setAdapter(ArrayAdapter(requireContext(),
                android.R.layout.simple_list_item_1, scales).also { adapter ->
                autoCompleteScale.setAdapter(adapter)})

            binding.addModelImageView.setOnClickListener {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                modelPhotoLauncher.launch(intent)
            }

            val photoRecyclerView = binding.addModelRecyclerview
            val photoAdapter = SideScrollImageRecyclerAdapter({ deletedPhoto ->
                viewModel.addCurrentModelDeletedPhoto(deletedPhoto)
            }, canDeleteItems = true)
            photoRecyclerView.adapter = photoAdapter
            photoRecyclerView.layoutManager = LinearLayoutManager(view.context,LinearLayoutManager.HORIZONTAL, false)

            if (editing) {
                autoCompleteScale.setText(model.scale)
                binding.addModelAirlineEdittext.setText(model.airline)
                binding.addModelFrameEdittext.setText(model.frame)
                binding.addModelLiveryEdittext.setText(model.livery)
                autoCompleteBrand.setText(model.manufacturer)
                binding.modelRowCommentTextview.setText(model.comment)
                photoAdapter.photos = model.photos
            }

            viewModel.getSelectedModelMutableLiveData().observe(viewLifecycleOwner) { photosList ->
                photoAdapter.photos = photosList ?: arrayListOf()
                photoAdapter.notifyDataSetChanged()
            }

            binding.addModelBtnAdd.text = resources.getString(R.string.edit)
            binding.addModelBtnAdd.setOnClickListener {
                val scale = autoCompleteScale.text.toString()
                val airline = binding.addModelAirlineEdittext.text.toString()
                val frame = binding.addModelFrameEdittext.text.toString()
                val livery = binding.addModelLiveryEdittext.text.toString()
                val brand = autoCompleteBrand.text.toString()
                val comment = binding.modelRowCommentTextview.text.toString()
                val reg = binding.modelRowRegTextview.text.toString()

                val newPhotos = viewModel.getCurrentNonDeletedPhotos()
                // remove user deleted models from firebase
                viewModel.removeCurrentDeletedModelPhotos()

                if (editing) {
                    val editedModel = Model(
                        getUser()!!.uid, brand, brand, scale,
                        frame, airline, livery, newPhotos, comment, 0, model.id, model.reg
                    )
                    viewModel.updateModel(editedModel)
                }
                else{
                    val model = Model(
                        getUser()!!.uid, brand, brand, scale, frame, airline,
                        livery, photos, comment, 0, null, reg)
                    viewModel.uploadModel(model)
                }
                popup.dismiss()
            }
            popup.showAsDropDown(profileImageView, 0, 0)
        }

        val postRecyclerView = view.findViewById<RecyclerView>(R.id.profile_post_recycler)
        val postAdapter = PostRecyclerAdapter(
            // avatar clicked
            {
            // do not need to do anything here as all posts are from current user
            },
            //edit post button click
            { post ->
            parentFragmentManager.beginTransaction()
                .add(R.id.container, AddPostFragment(post, true))
                .addToBackStack("home").hide(this).commit()
            },
            // delete post button click
            { post ->
                viewModel.deletePost(post.id)
            },
            //comment button clicked
            { post ->
                parentFragmentManager.beginTransaction()
                    .add(R.id.container, ViewPostFragment(post)).addToBackStack("home")
                    .hide(this).commit()
            },
            //reaction selected
            { pair ->
                val (reaction, pid) = pair
                viewModel.addReact(reaction, pid)
            })
        val postLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context)
        postRecyclerView.layoutManager = postLayoutManager
        postRecyclerView.adapter = postAdapter

        val modelsRecyclerView = view.findViewById<RecyclerView>(R.id.profile_model_recycler)
        val modelAdapter = ModelRecyclerAdapter(
            //model edited
            { model, _ ->
                editModel(model)
            },
            //model deleted
            { model ->
                viewModel.deleteModel(model.id!!)
            },
        )
        val modelLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context)
        modelsRecyclerView.layoutManager = modelLayoutManager
        modelsRecyclerView.adapter = modelAdapter
        val friendsRecyclerView = view.findViewById<RecyclerView>(R.id.profile_friend_recycler)
        val friendRecyclerAdapter = FriendRecyclerAdapter(
            //accept friend button clicked
            { user ->
                viewModel.addFriend(user)
            },
            //decline friend button clicked
            { user ->
                val token = user.requestToken
                //TODO decline

            },
            //go to friend profile
            { user ->
                val uid = user.id
                parentFragmentManager.beginTransaction()
                    .add(R.id.container, ProfileFragment(uid)).addToBackStack("home")
                    .remove(this).commit()
            }
        )
        val friendLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context)
        friendsRecyclerView.adapter = friendRecyclerAdapter
        friendsRecyclerView.layoutManager = friendLayoutManager

        profileTogglePosts.setOnClickListener {
            postRecyclerView.visibility = View.VISIBLE
            modelsRecyclerView.visibility = View.GONE
            friendsRecyclerView.visibility = View.GONE
            profileAddModelButton.visibility = View.GONE
            profileAddFriendButton.visibility = View.GONE
        }
        profileToggleModels.setOnClickListener {
            postRecyclerView.visibility = View.GONE
            modelsRecyclerView.visibility = View.VISIBLE
            friendsRecyclerView.visibility = View.GONE
            profileAddFriendButton.visibility = View.GONE
            if (profileUserId == getUser()!!.uid) {
                profileAddModelButton.visibility = View.VISIBLE
            }
        }
        //todo hide friend button if friends already
        profileToggleFriends.setOnClickListener {
            postRecyclerView.visibility = View.GONE
            modelsRecyclerView.visibility = View.GONE
            friendsRecyclerView.visibility = View.VISIBLE
            profileAddModelButton.visibility = View.GONE
            profileAddFriendButton.visibility = View.VISIBLE
        }

        if (profileUserId != getUser()!!.uid) {
            profileEditButton.visibility = View.GONE
        }
        else if (profileUserId == getUser()!!.uid) {
            profileEditButton.visibility = View.VISIBLE
        }

        profileAddFriendButton.setOnClickListener {
            if (profileUserId == getUser()!!.uid) {
                //launch token generator popup
                val context = view.context

                val addFriendPopupInflater: LayoutInflater  =
                    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val binding = PopupAddFriendBinding.inflate(addFriendPopupInflater)
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

                binding.addFriendCodeTextview.text = viewModel.getFriendRequestToken()

                binding.addFriendBtnAdd.setOnClickListener {
                    val token = binding.addFriendEnterCodeEditText.text.toString()
                    viewModel.addFriendFromToken(token)
                    viewModel.getAddFriendFromTokenResponseMutableLiveData()
                        .observe(viewLifecycleOwner) { result ->
                            val snackbar = Snackbar
                                .make(view, result, Snackbar.LENGTH_LONG)
                            snackbar.show()
                            popup.dismiss()
                        }
                }
                popup.showAsDropDown(profileImageView, 0, 0)

            }
            else if (profileUserId != getUser()!!.uid){
                //directly send friend request
                viewModel.sendFriendRequest(profileUserId)
                val snackbar = Snackbar
                    .make(view, "Friend request sent", Snackbar.LENGTH_LONG)
                snackbar.show()
            }
        }

        profileAddModelButton.setOnClickListener {
            val context = view.context
            viewModel.clearCurrentModelPhotos()

            val addModelPopupInflater: LayoutInflater  =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val binding = PopupAddModelBinding.inflate(addModelPopupInflater)
            val popup = PopupWindow(
                binding.root,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            // Closes the popup window when touch outside.
            popup.isOutsideTouchable = true
            popup.isFocusable = true

            // Removes default background.
            popup.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            // brand autocomplete
            val autoCompleteBrand = binding.addModelAutocompleteBrand
            val brands: Array<out String> = resources.getStringArray(R.array.brands_array)
            autoCompleteBrand.setAdapter(ArrayAdapter(requireContext(),
                android.R.layout.simple_list_item_1, brands).also { adapter ->
                autoCompleteBrand.setAdapter(adapter)})

            // scale autocomplete
            val autoCompleteScale = binding.addModelAutocompleteScale
            val scales: Array<out String> = resources.getStringArray(R.array.scales_array)
            autoCompleteScale .setAdapter(ArrayAdapter(requireContext(),
                android.R.layout.simple_list_item_1, scales).also { adapter ->
                autoCompleteScale.setAdapter(adapter)})

            binding.addModelImageView.setOnClickListener {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                modelPhotoLauncher.launch(intent)
            }

            val photoRecyclerView = binding.addModelRecyclerview
            val photoAdapter = SideScrollImageRecyclerAdapter({ deletedPhoto ->
                viewModel.addCurrentModelDeletedPhoto(deletedPhoto)
            },canDeleteItems = true)
            photoRecyclerView.adapter = photoAdapter
            photoRecyclerView.layoutManager = LinearLayoutManager(view.context,LinearLayoutManager.HORIZONTAL, false)

            viewModel.getSelectedModelMutableLiveData().observe(viewLifecycleOwner) { photosList ->

                photoAdapter.photos = photosList ?: arrayListOf()
                photoAdapter.notifyDataSetChanged()
            }

            binding.addModelBtnAdd.setOnClickListener {
                val scale = autoCompleteScale.text.toString()
                val airline = binding.addModelAirlineEdittext.text.toString()
                val frame = binding.addModelFrameEdittext.text.toString()
                val livery = binding.addModelLiveryEdittext.text.toString()
                val brand = autoCompleteBrand.text.toString()
                val comment = binding.modelRowCommentTextview.text.toString()
                val reg = binding.modelRowRegTextview.text.toString()

                val newPhotos = viewModel.getCurrentNonDeletedPhotos()

                val model = Model(
                    getUser()!!.uid, brand, brand, scale, frame, airline,
                    livery, newPhotos, comment, 0, null, reg)
                viewModel.uploadModel(model)
                popup.dismiss()
            }
            popup.showAsDropDown(profileImageView, 0, 0)
        }

        viewModel.fetchPosts.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                    val posts = result.data
                    if (posts != null) {
                        postAdapter.setData(posts)
                    }
                }

                is Response.Failure -> {
                    Toast.makeText(context, "Failed to load posts, please try again later",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.fetchModels.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                    result.data?.let { modelAdapter.setData(it) }
                }

                is Response.Failure -> {
                    Toast.makeText(context, "Failed to load models, please try again later",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }

        //only grab friend requests if user is on their own profile
/*        if (profileUserId == getUser()!!.uid) {
            viewModel.fetchFriendsAndRequests.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Response.Loading -> {
                    }
                    is Response.Success -> {
                        val users = result.data
                        friendRecyclerAdapter.users = users ?: arrayListOf()
                        modelAdapter.notifyDataSetChanged()

                    }

                    is Response.Failure -> {

                    }
                }
            }
        }
        else {
            //different user, just grab friends
            viewModel.fetchFriends.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is Response.Loading -> {
                    }
                    is Response.Success -> {
                        val users = result.data
                        friendRecyclerAdapter.users = users ?: arrayListOf()
                        modelAdapter.notifyDataSetChanged()

                    }

                    is Response.Failure -> {

                    }
                }
            }
        }*/

        viewModel.getFriendsMutableLiveData().observe(viewLifecycleOwner) { friendsList ->
            friendRecyclerAdapter.users = friendsList
            friendRecyclerAdapter.notifyDataSetChanged()
        }

        viewModel.getUserBioMutableLiveData().observe(viewLifecycleOwner) { bio->
            profileBioText.text = bio
        }
        viewModel.getUsernameMutableLiveData().observe(viewLifecycleOwner) { name->
            profileUsername.text = name
        }
        viewModel.getAvatarMutableLiveData().observe(viewLifecycleOwner) { uri->
            Glide.with(view).load(uri)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(profileImageView)
            if (imageAdded){
                Glide.with(view).load(uri).into(profileEditAvatar)
            }
        }
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

                    val valueAccent = TypedValue()
                    requireContext().theme.resolveAttribute(android.R.attr.colorAccent, valueAccent, true)
                    val colorAccent = valueAccent.data

                    val valueBackground = TypedValue()
                    requireContext().theme.resolveAttribute(android.R.attr.colorBackgroundFloating, valueBackground, true)
                    val colorBackground = valueBackground.data

                    options.setCropGridColor(colorAccent)
                    options.setCropFrameColor(colorAccent)
                    options.setToolbarColor(colorAccent)
                    options.setActiveControlsWidgetColor(colorAccent)
                    options.setLogoColor(colorAccent)
                    options.setRootViewBackgroundColor(colorBackground)
                    options.setDimmedLayerColor(Color.TRANSPARENT)
                    options.setToolbarWidgetColor(colorBackground)
                    options.setStatusBarColor(colorBackground)

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
                        delay(2000)
                        profileEditButton.visibility = View.VISIBLE
                    }
                    profileSaveButton.visibility = View.GONE

                    // check if bio has been edited
                    if (profileBioText.text.toString() != profileBioEditText.text.toString()) {
                        profileBioText.text = profileBioEditText.text.toString()
                        viewModel.updateBio(profileBioEditText.text.toString())
                    }
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
            val cropError: Throwable?  = data?.let { UCrop.getError(it) }
        }
        //super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}