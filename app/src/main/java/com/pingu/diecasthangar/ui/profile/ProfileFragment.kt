package com.pingu.diecasthangar.ui.profile

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.*
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.common.util.DeviceProperties
import com.pingu.diecasthangar.R
import com.pingu.diecasthangar.data.model.Model
import com.pingu.diecasthangar.data.model.Photo
import com.pingu.diecasthangar.data.model.User
import com.pingu.diecasthangar.data.remote.Response
import com.pingu.diecasthangar.data.remote.getUser
import com.pingu.diecasthangar.databinding.FragmentProfileBinding
import com.pingu.diecasthangar.databinding.PopupAddFriendBinding
import com.pingu.diecasthangar.databinding.PopupAddModelBinding
import com.pingu.diecasthangar.databinding.PopupViewModelPhotosBinding
import com.pingu.diecasthangar.ui.dashboard.DashboardViewModel
import com.pingu.diecasthangar.ui.viewpost.ViewPostFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.pingu.diecasthangar.ui.*
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
    private var currentTab: String = "posts"
    private val dashViewModel: DashboardViewModel by activityViewModels()
    private var editing = false

    //todo use for popup
    private val handler = Handler(Looper.getMainLooper())


    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            currentTab = savedInstanceState.getString("TAB") ?: "posts"
        }
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        //frameLayout = FrameLayout(requireActivity())
        val view = binding.root
        requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val orientation = resources.configuration.orientation
        val isTablet: Boolean = DeviceProperties.isTablet(requireContext())
        val fragContainer =
            if (isTablet){
                R.id.right_container
            } else {
                R.id.container
            }
        // Inflate the layout for this fragment

        val profileImageView: ImageView = binding.profileAvatar
        val profileEditAvatar: ImageView = binding.profileEditAvatar
        val profileUsername: TextView = binding.profileName
        val profileBioText: TextView = binding.profileTextBio
        val profileBioEditText: EditText = binding.profileEditTextBio
        val profileEditButton: ImageButton = binding.profileBtnEdit
        val profileSaveButton: FloatingActionButton = binding.profileBtnSave
        val profileTogglePosts: Button = binding.profileBtnPosts
        val profileToggleModels: Button = binding.profileBtnModels
        val profileToggleFriends: Button = binding.profileBtnFriends
        val profileAddModelButton: FloatingActionButton = binding.profileBtnAddModel
        val profileAddFriendButton: FloatingActionButton = binding.profileBtnAddFriend
        val profileLayout = binding.profileLay

        val postRecyclerView = binding.profilePostRecycler
        val postLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context)
        postRecyclerView.layoutManager = postLayoutManager

        val friendsRecyclerView = binding.profileFriendRecycler
        val friendsLayoutManager = LinearLayoutManager(context)
        friendsRecyclerView.layoutManager = friendsLayoutManager

        fun navigateToFragment(fragment: Fragment){
            if (isTablet){
                parentFragmentManager.beginTransaction()
                    .replace(fragContainer, fragment).addToBackStack("home")
                    .commit()
            }
            else {
                parentFragmentManager.beginTransaction()
                    .add(fragContainer, fragment).addToBackStack("home")
                    .hide(this).commit()
            }
        }
        val postAdapter = PostRecyclerAdapter(
            // avatar clicked
            {
                // do not need to do anything here as all posts are from current user
            },
            //edit post button click
            { post ->
                navigateToFragment(AddPostFragment(post,true))
            },
            // delete post button click
            { post ->
                viewModel.deletePost(post.id)
            },
            //comment button clicked
            { post ->
                dashViewModel.currentViewingPost = post
                navigateToFragment(ViewPostFragment(post))
            },
            //reaction selected
            { pair ->
                val (reaction, pid) = pair
                viewModel.addReact(reaction, pid)
            })
        postRecyclerView.adapter = postAdapter



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
                navigateToFragment(ProfileFragment(uid))
            }
        )

        friendsRecyclerView.adapter = friendRecyclerAdapter

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

        fun observePosts() {
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
                        Toast.makeText(
                            context, "Failed to load posts, please try again later",
                            LENGTH_SHORT).show()
                        Log.e("loading","Post failed to load: ${result.e}")
                    }
                    else -> {}
                }
            }
        }
        observePosts()

        @SuppressLint("NotifyDataSetChanged")
        fun editModel(model: Model, editing: Boolean = true) {
            this.editing = true
            val context = view.context
            viewModel.clearCurrentModelPhotos()
            if (editing) {
                viewModel.addCurrentModelPhotos(model.photos)
            }

            val addModelPopupInflater: LayoutInflater =
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
            autoCompleteBrand.setAdapter(ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1, brands
            ).also { adapter ->
                autoCompleteBrand.setAdapter(adapter)
            })

            // scale autocomplete
            val autoCompleteScale = binding.addModelAutocompleteScale
            val scales: Array<out String> = resources.getStringArray(R.array.scales_array)
            autoCompleteScale.setAdapter(ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1, scales
            ).also { adapter ->
                autoCompleteScale.setAdapter(adapter)
            })

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
            photoRecyclerView.layoutManager =
                LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)

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
                } else {
                    val model = Model(
                        getUser()!!.uid, brand, brand, scale, frame, airline,
                        livery, photos, comment, 0, null, reg
                    )
                    viewModel.uploadModel(model)
                }
                popup.dismiss()
                this.editing = false
            }
            popup.showAtLocation(view, 0, 0,0)
        }


        fun showModelPhotoPopup(model: Model){
            viewModel.currentModelViewing = model
            val height =
                when (orientation) {
                    Configuration.ORIENTATION_PORTRAIT -> {
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    }
                    ORIENTATION_LANDSCAPE -> {
                        ViewGroup.LayoutParams.MATCH_PARENT
                    }
                    else -> {
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    }
                }
            val context = view.context
            val popupInflater: LayoutInflater  =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupBinding = PopupViewModelPhotosBinding.inflate(popupInflater)
            val popup = PopupWindow(
                popupBinding.root,
                WindowManager.LayoutParams.MATCH_PARENT,
                height

            )
            //popup.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val photoRecyclerView = popupBinding.modelPopupRecyclerview
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                val photoAdapter =  SideScrollImageRecyclerAdapter({
                    //display only mode
                }, false)
                photoRecyclerView.adapter = photoAdapter
                photoRecyclerView.layoutManager = LinearLayoutManager(context,
                    LinearLayoutManager.HORIZONTAL, false)
                photoAdapter.photos = model.photos
            }
            else {
                val photoAdapter =  FullScreenImageRecyclerAdapter()
                photoRecyclerView.adapter = photoAdapter
                photoRecyclerView.layoutManager = LinearLayoutManager(context,
                    LinearLayoutManager.HORIZONTAL, false)
                photoAdapter.photos = model.photos

            }
            popupBinding.modelPopupExit.setOnClickListener {
                viewModel.currentModelViewing = null
                popup.dismiss()
            }
            popup.showAtLocation(view, Gravity.CENTER, 0, 0)
        }


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
            { model ->
                // fullscreen display model photos
                showModelPhotoPopup(model)
            }
        )

        val modelLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context)
        modelsRecyclerView.layoutManager = modelLayoutManager
        modelsRecyclerView.adapter = modelAdapter

        fun swapToModelsTab(){
            postRecyclerView.visibility = View.GONE
            modelsRecyclerView.visibility = View.VISIBLE
            friendsRecyclerView.visibility = View.GONE
            profileAddFriendButton.visibility = View.GONE
            if (profileUserId == getUser()!!.uid) {
                profileAddModelButton.visibility = View.VISIBLE
            }
            currentTab = "models"
        }

        fun swapToPostsTab(){
            postRecyclerView.visibility = View.VISIBLE
            modelsRecyclerView.visibility = View.GONE
            friendsRecyclerView.visibility = View.GONE
            profileAddModelButton.visibility = View.GONE
            profileAddFriendButton.visibility = View.GONE
            currentTab = "posts"
        }

        fun swapToFriendsTab(){
            postRecyclerView.visibility = View.GONE
            modelsRecyclerView.visibility = View.GONE
            friendsRecyclerView.visibility = View.VISIBLE
            profileAddModelButton.visibility = View.GONE
            profileAddFriendButton.visibility = View.VISIBLE
            //if not on own profile, and already friends,hide button
            val currentUser = User(id = getUser()!!.uid, "", "")
            if (friendRecyclerAdapter.users.contains(currentUser) && profileUserId != currentUser.id) {
                profileAddFriendButton.visibility = View.GONE
            }
            currentTab = "friends"
        }

        when (currentTab) {
            "models" -> swapToModelsTab()
            "posts" -> swapToPostsTab()
            "friends" -> swapToFriendsTab()
            //else -> swapToAllTab()
        }
        profileToggleFriends.setOnClickListener {
            swapToFriendsTab()
        }
        profileToggleModels.setOnClickListener {
            swapToModelsTab()
        }
        profileTogglePosts.setOnClickListener {
            swapToPostsTab()
        }


        if (profileUserId != getUser()!!.uid) {
            profileEditButton.visibility = View.GONE
        } else if (profileUserId == getUser()!!.uid) {
            profileEditButton.visibility = View.VISIBLE
        }

        profileAddFriendButton.setOnClickListener {
            if (profileUserId == getUser()!!.uid) {
                //launch token generator popup
                val context = view.context

                val addFriendPopupInflater: LayoutInflater =
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

                //add to clipboard on tap
                binding.addFriendCodeTextview.setOnClickListener {
                    val clipboard: ClipboardManager? =
                        context.getSystemService() as ClipboardManager?
                    val clip = ClipData.newPlainText("token", viewModel.getFriendRequestToken())
                    clipboard?.setPrimaryClip(clip)
                    val snackbar = Snackbar
                        .make(view, "Copied to Clipboard", LENGTH_SHORT)
                    snackbar.show()
                }

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

            } else if (profileUserId != getUser()!!.uid) {
                //directly send friend request
                viewModel.sendFriendRequest(profileUserId)
                val snackbar = Snackbar
                    .make(view, "Friend request sent", Snackbar.LENGTH_LONG)
                snackbar.show()
            }
        }

        profileAddModelButton.setOnClickListener {
            this.editing = true
            val context = view.context
            viewModel.clearCurrentModelPhotos()

            val addModelPopupInflater: LayoutInflater =
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
            autoCompleteBrand.setAdapter(ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1, brands
            ).also { adapter ->
                autoCompleteBrand.setAdapter(adapter)
            })

            // scale autocomplete
            val autoCompleteScale = binding.addModelAutocompleteScale
            val scales: Array<out String> = resources.getStringArray(R.array.scales_array)
            autoCompleteScale.setAdapter(ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1, scales
            ).also { adapter ->
                autoCompleteScale.setAdapter(adapter)
            })

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
            photoRecyclerView.layoutManager =
                LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)

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
                    livery, newPhotos, comment, 0, null, reg
                )
                viewModel.uploadModel(model)
                popup.dismiss()
                viewModel.currentModelEditing = null
                this.editing = false
            }
            popup.showAsDropDown(profileImageView, 0, 0)
        }

        viewModel.fetchModels.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                    result.data?.let { modelAdapter.setData(it) }
                }

                is Response.Failure -> {
                    Toast.makeText(
                        context, "Failed to load models, please try again later",
                        LENGTH_SHORT
                    ).show()
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
                    requireContext().theme.resolveAttribute(android.R.attr.colorBackground, valueBackground, true)
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
                profileLayout.visibility = View.VISIBLE

            }
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

                if (orientation == ORIENTATION_LANDSCAPE){
                    profileLayout.visibility = View.GONE
                }

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

        //restore photo viewer popup after screen rotate
        if (viewModel.currentModelViewing != null){
            binding.root.post{
                showModelPhotoPopup(viewModel.currentModelViewing!!)
            }
        }
        super.onViewCreated(view, savedInstanceState)
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
            Log.e("CROP","Error cropping profile avatar: $cropError")
        }
        //super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        //save UI state
        outState.putString("TAB", currentTab)
        outState.putBoolean("editing", editing)
        super.onSaveInstanceState(outState)
        //outState.putString("avatar-uri", )

    }
}