package com.example.diecasthangar.ui.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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
import com.example.diecasthangar.R
import com.example.diecasthangar.core.util.loadingDummyPost
import com.example.diecasthangar.data.model.Model
import com.example.diecasthangar.data.model.Photo
import com.example.diecasthangar.databinding.FragmentProfileBinding
import com.example.diecasthangar.databinding.PopupAddFriendBinding
import com.example.diecasthangar.databinding.PopupAddModelBinding
import com.example.diecasthangar.ui.PostRecyclerAdapter
import com.example.diecasthangar.domain.remote.getUser
import com.example.diecasthangar.ui.AddPostFragment
import com.example.diecasthangar.ui.UserViewModel
import com.example.diecasthangar.ui.ViewPostFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


open class ProfileFragment(uid: String = getUser()!!.uid): Fragment(), LifecycleOwner {
    private val profileUserId = uid
    var imageAdded = false
    val viewModel: ProfileViewModel by viewModels { ProfileViewModel.Factory(profileUserId) }

    private var _binding: FragmentProfileBinding? = null
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("NotifyDataSetChanged")
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
        val profileLayout: LinearLayout = binding.profileLayout
        val profileTogglePosts: Button = binding.profileBtnPosts
        val profileToggleModels: Button = binding.profileBtnModels
        val profileToggleFriends: Button = binding.profileBtnFriends
        val profileAddModelButton: FloatingActionButton = binding.profileBtnAddModel
        val profileAddFriendButton: FloatingActionButton = binding.profileBtnAddFriend

        profileLayout.setOnClickListener{
            //capture click to avoid clicking on background fragment
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
                .add(R.id.container, AddPostFragment(post, true)).addToBackStack("home")
                .commit()
            },
            // delete post button click
            { post ->
                viewModel.deletePost(post.id)
            },
            //comment button clicked
            { post ->
                parentFragmentManager.beginTransaction()
                    .add(R.id.container, ViewPostFragment(post)).addToBackStack("home")
                    .commit()
            })
        val postLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context)
        postRecyclerView.layoutManager = postLayoutManager
        postRecyclerView.adapter = postAdapter

        var loading = true
        val loadingPost = loadingDummyPost()
        postAdapter.posts.add(loadingPost)

        val modelsRecyclerView = view.findViewById<RecyclerView>(R.id.profile_model_recycler)
        val modelAdapter = ModelRecyclerAdapter()
        val modelLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context)
        modelsRecyclerView.layoutManager = modelLayoutManager
        modelsRecyclerView.adapter = modelAdapter
        val friendsRecyclerView = view.findViewById<RecyclerView>(R.id.profile_friend_recycler)
        val friendRecyclerAdapter = FriendRecyclerAdapter(
            //accept friend button clicked
            { user ->
                val uid = user.id
                viewModel.addFriend(user)

            },
            //decline friend button clicked
            { user ->
                val uid = user.id
                //TODO decline

            },
            //go to friend profile
            { user ->
                val uid = user.id
                parentFragmentManager.beginTransaction()
                    .add(R.id.container, ProfileFragment(uid)).addToBackStack("home").hide(this)
                    .commit()


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

        val photos = ArrayList<Photo>()
        val modelPhotoLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK
                && result.data != null
            ) {
                if (result.data!!.clipData != null) {
                    // reset list
                    val mClipData = result.data!!.clipData
                    for (i in 0 until mClipData!!.itemCount) {
                        val item = mClipData.getItemAt(i)
                        val imageUri = item.uri
                        val photo = Photo(localUri = imageUri, remoteUri = imageUri.toString())
                        photos.add(photo)
                    }
                } else if (result.data!!.data != null) {
                    val imageUri = result.data!!.data!!
                    val photo = Photo(localUri = imageUri, remoteUri = imageUri.toString())
                    photos.add(photo)
                }
            }
        }


        profileAddModelButton.setOnClickListener {
            val context = view.context

            val addModelPopupInflater: LayoutInflater  =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val binding = PopupAddModelBinding.inflate(addModelPopupInflater)
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

            binding.addModelBtnAdd.setOnClickListener {
                val scale = autoCompleteScale.text.toString()
                val airline = binding.addModelAirlineEdittext.text.toString()
                val frame = binding.addModelFrameEdittext.text.toString()
                val livery = binding.addModelLiveryEdittext.text.toString()
                val brand = autoCompleteBrand.text.toString()
                val comment = binding.modelRowCommentTextview.text.toString()

                val model = Model(
                    brand,
                    brand,
                    scale,
                    frame,
                    airline,
                    livery,
                    photos,
                    comment,
                    0,
                    null)
                viewModel.uploadModel(model)
                modelAdapter.models.add(model)
                modelAdapter.notifyItemInserted(modelAdapter.models.size -1)
                popup.dismiss()
            }
            popup.showAsDropDown(profileImageView, 0, 0)
        }



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

        viewModel.getFriendsMutableLiveData().observe(viewLifecycleOwner) { friendsList ->
            friendRecyclerAdapter.users = friendsList
            friendRecyclerAdapter.notifyDataSetChanged()

        }

        viewModel.getModelsMutableLiveData().observe(viewLifecycleOwner) { modelsList ->
            modelAdapter.models = modelsList ?: arrayListOf()
            modelAdapter.notifyDataSetChanged()
        }

        viewModel.getUserBioMutableLiveData().observe(viewLifecycleOwner) { bio->
            profileBioText.text = bio
        }
        viewModel.getUsernameMutableLiveData().observe(viewLifecycleOwner) { name->
            profileUsername.text = name
        }
        viewModel.getAvatarMutableLiveData().observe(viewLifecycleOwner) { uri->
            Glide.with(view).load(uri).into(profileImageView)
            if (imageAdded){
                Glide.with(view).load(uri).into(profileEditAvatar)
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
                    val lightColor = ContextCompat.getColor(requireContext(),com.google.android.material.R.color.material_dynamic_primary60)
                    val darkColor = ContextCompat.getColor(requireContext(),com.google.android.material.R.color.material_dynamic_primary40)
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
                        delay(2000)
                        profileEditButton.visibility = View.VISIBLE
                    }
                    profileSaveButton.visibility = View.GONE

                    // prevent unneeded api call
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
            val cropError: Throwable?  = data?.let { UCrop.getError(it) };
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}