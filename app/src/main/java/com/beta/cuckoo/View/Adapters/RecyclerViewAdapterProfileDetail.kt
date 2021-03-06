package com.beta.cuckoo.View.Adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.beta.cuckoo.Model.CuckooPost
import com.beta.cuckoo.Model.PostPhoto
import com.beta.cuckoo.Model.User
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.MessageRepositories.MessageRepository
import com.beta.cuckoo.Repository.PostRepositories.PostRepository
import com.beta.cuckoo.Repository.UserRepositories.FollowRepository
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.beta.cuckoo.View.Chat.Chat
import com.beta.cuckoo.View.MainMenu.MainMenu
import com.beta.cuckoo.View.PostDetail.PostDetail
import com.beta.cuckoo.View.Profile.ProfileDetail
import com.beta.cuckoo.View.Profile.ProfileSetting
import com.beta.cuckoo.View.UserInfoView.UserShow
import com.bumptech.glide.Glide
import com.google.gson.Gson

class RecyclerViewAdapterProfileDetail (arrayOfPhotos: ArrayList<PostPhoto>, userObject: User, activity: ProfileDetail, currentUser: Boolean,
                                        userRepository: UserRepository, postRepository: PostRepository, messageRepository: MessageRepository, followRepository: FollowRepository, isPrivateProfile: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // Array of photos created by the user
    private val arrayOfPhotos = arrayOfPhotos

    // User object of the selected user
    private val userObject = userObject

    // Activity of the parent activity
    private val activity = activity

    // The user object is current user or not? (this var will keep track of that)
    private val currentUser = currentUser

    // User repository
    private val userRepository = userRepository

    // Post repository
    private val postRepository = postRepository

    // Message repository
    private val messageRepository = messageRepository

    // Follow repository
    private val followRepository = followRepository

    // Private profile status
    private val isPrivateProfile = isPrivateProfile

    //*********************************** VIEW HOLDERS FOR THE RECYCLER VIEW ***********************************
    // ViewHolder for the profile detail page header
    inner class ViewHolderProfileDetailHeader internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Components from the layout
        private val userAvatar : ImageView = itemView.findViewById(R.id.userAvatarProfileDetail)
        private val userCover : ImageView = itemView.findViewById(R.id.profileDetailCoverPhoto)
        private val userFullName : TextView = itemView.findViewById(R.id.userFullNameProfileDetail)
        private val bio : TextView = itemView.findViewById(R.id.userBioProfileDetail)
        private val numOfPosts : TextView = itemView.findViewById(R.id.numOfPosts)
        private val numOfFollowers : TextView = itemView.findViewById(R.id.numOfFollowers)
        private val numOfFollowing : TextView = itemView.findViewById(R.id.numOfFollowing)
        private val numOfFollowersView : ConstraintLayout = itemView.findViewById(R.id.numOfFollowersView)
        private val numOfFollowingsView : ConstraintLayout = itemView.findViewById(R.id.numOfFollowingsView)

        // The function to set up header row for the profile detail page
        fun setUpProfileDetailHeaderPage (userObject: User) {
            // Load full name into the TextView
            userFullName.text = userObject.getFullName()

            // Load user bio into the TextView
            bio.text = userObject.getBio()

            // Set on click listener for the num of followers view
            numOfFollowersView.setOnClickListener {
                // If profile is not private, show list of followers
                if (!isPrivateProfile) {
                    // Take user to the activity where the user can see list of followers of the user
                    // The intent object
                    val intent = Intent(activity, UserShow::class.java)

                    // Set post id to that activity and tell it to show list of likes of the post
                    intent.putExtra("whatToDo", "getListOfFollowers")
                    intent.putExtra("postId", "")
                    intent.putExtra("userId", userObject.getId())

                    // Start the activity
                    activity.startActivity(intent)
                }
            }

            // Set on click listener for the num of followings view
            numOfFollowingsView.setOnClickListener {
                // If profile is not private, show list of following
                if (!isPrivateProfile) {
                    // Take user to the activity where the user can see list of followings of the user
                    // The intent object
                    val intent = Intent(activity, UserShow::class.java)

                    // Set post id to that activity and tell it to show list of likes of the post
                    intent.putExtra("whatToDo", "getListOfFollowings")
                    intent.putExtra("postId", "")
                    intent.putExtra("userId", userObject.getId())

                    // Start the activity
                    activity.startActivity(intent)
                }
            }

            // Load avatar of the user into the ImageView
            Glide.with(activity)
                .load(userObject.getAvatarURL())
                .into(userAvatar)

            // Load cover photo into the ImageView
            Glide.with(activity)
                .load(userObject.getCoverURL())
                .into(userCover)

            // Call the function to get number of followers of the user
            getNumOfFollowers(userObject.getId(), numOfFollowers)

            // Call the function to get number of followings of the user
            getNumOfFollowings(userObject.getId(), numOfFollowing)

            // Call the function to get number of posts of the user
            getNumOfPosts(userObject.getId(), numOfPosts)
        }
    }

    // ViewHolder for the edit profile button
    inner class ViewHolderEditProfileButton internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Components from the layout
        private val editProfileButton : Button = itemView.findViewById(R.id.editProfileProfileDetail)

        // The function to set up the edit profile button
        fun setUpEditProfileButton () {
            // Add on click listener to the edit profile button which will take user to the activity where the user can edit profile
            editProfileButton.setOnClickListener {
                // Go to the activity where user can see profile settings
                val intent = Intent(activity, ProfileSetting::class.java)
                activity.startActivity(intent)
            }
        }
    }

    // ViewHolder for the follow/message button
    inner class ViewHolderFollowMessageButton internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Components from the layout
        private val followUnfollowButton : Button = itemView.findViewById(R.id.followUnfollowButtonProfileDetail)
        private val messageButton : Button = itemView.findViewById(R.id.sendMessageButtonProfileDetail)

        // The function to set up the follow/message button
        fun setUpFollowMessageButton (userObject: User) {
            // Set on click listener for the follow/unfollow button
            followUnfollowButton.setOnClickListener {
                // Check content of the button
                // If content of the button is "Follow", create a new follow object between the 2 users
                if (followUnfollowButton.text == "Follow") {
                    // Call the function to create follow for the user
                    createNewFollow(userObject.getId(), followUnfollowButton)
                } // Otherwise, remove a follow object between the 2 users
                else {
                    removeFollow(userObject.getId(), followUnfollowButton)
                }
            }

            // Set on click listener for the message button
            messageButton.setOnClickListener {
                // Call the function to take user to the activity where the user can start chatting
                getInfoOfCurrentUserAndGotoChat()
            }

            // Call the function to load follow status between the 2 users and set right content for the follow button
            getFollowStatus(userObject.getId(), followUnfollowButton)
        }
    }

    // ViewHolder for the user album
    inner class ViewHolderUserAlbum internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Components from the layout
        private val image1ImageView: ImageView = itemView.findViewById(R.id.image1ProfileDetail)
        private val image2ImageView: ImageView = itemView.findViewById(R.id.image2ProfileDetail)
        private val image3ImageView: ImageView = itemView.findViewById(R.id.image3ProfileDetail)
        private val image4ImageView: ImageView = itemView.findViewById(R.id.image4ProfileDetail)

        // The function to set up user album row
        fun setUpUserAlbumRow (image1URL: String, image2URL: String, image3URL: String, image4URL: String,
                                image1PostId: String, image2PostId: String, image3PostId: String, image4PostId: String) {
            // Load images into the ImageView
            if (image1URL != "") {
                Glide.with(activity).load(image1URL).into(image1ImageView)
            }
            if (image2URL != "") {
                Glide.with(activity).load(image2URL).into(image2ImageView)
            }
            if (image3URL != "") {
                Glide.with(activity).load(image3URL).into(image3ImageView)
            }
            if (image4URL != "") {
                Glide.with(activity).load(image4URL).into(image4ImageView)
            }

            // Set on click listener for the image view so that it will take user to the activity where the user
            // can see post detail of the post associated with the photo
            image1ImageView.setOnClickListener {
                // Call the function
                getPostObjectBasedOnIdAndGotoPostDetail(image1PostId)
            }
            image2ImageView.setOnClickListener {
                // Call the function
                getPostObjectBasedOnIdAndGotoPostDetail(image2PostId)
            }
            image3ImageView.setOnClickListener {
                // Call the function
                getPostObjectBasedOnIdAndGotoPostDetail(image3PostId)
            }
            image4ImageView.setOnClickListener {
                // Call the function
                getPostObjectBasedOnIdAndGotoPostDetail(image4PostId)
            }
        }
    }

    // ViewHolder for the private profile row
    inner class ViewHolderPrivateProfile internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // The function to set up the private profile row
        fun setUpPrivateProfileRow () { }
    }
    //*********************************** END VIEW HOLDERS FOR THE RECYCLER VIEW ***********************************

    //*********************************** GET USER INFO SEQUENCE ***********************************
    /*
    In this sequence, we will get these info
    1. Number of followers
    2. Number of followings
    3. Number of posts
     */

    // The function to get number of followers of the user
    fun getNumOfFollowers (userId: String, numOfFollowerTextView: TextView) {
        // Call the function to get number of followers of user with specified user id
        userRepository.getListOfFollowers(userId) {listOfUserId ->
            // Load number of followers into the text view
            numOfFollowerTextView.text = "${listOfUserId.size}"
        }
    }

    // The function to get number of following
    fun getNumOfFollowings (userId: String, numOfFollowingTextView: TextView) {
        // Call the function to get number of followings of user with specified user id
        userRepository.getListOfFollowing(userId) {listOfUserId ->
            // Load number of followers into the text view
            numOfFollowingTextView.text = "${listOfUserId.size}"
        }
    }

    // The function to get number of posts created by the user
    fun getNumOfPosts (userId: String, numOfPostsTextView: TextView) {
        // Call the function to get number of posts created by the user
        postRepository.getNumOfPostsCreatedByUserWithId(userId) {numOfPosts ->
            // Load number of posts into the text view
            numOfPostsTextView.text = "$numOfPosts"
        }
    }
    //*********************************** END GET USER INFO SEQUENCE ***********************************

    //*********************************** GO TO CHAT SEQUENCE ***********************************
    // The function to get info of the current user
    fun getInfoOfCurrentUserAndGotoChat () {
        // Call the function to check for chat room between the 2 users
        messageRepository.checkChatRoomBetween2Users(userObject.getId()) {chatRoomId ->
            // If the status is success, get message room id and pass it to the next view controller
            if (chatRoomId != "") {
                // Call the function and go to chat activity and let it know the chat room id as well
                gotoChat(userObject.getId(), chatRoomId)
            } // Otherwise, go to the chat activity and let the chat room id be blank
            else {
                gotoChat(userObject.getId(), "")
            }
        }
    }

    // The function which will take user to the activity where the user can start chatting
    private fun gotoChat (messageReceiverUserId: String, messageRoomId: String) {
        // The intent object
        val intent = Intent(activity, Chat::class.java)

        // Pass info to the chat activity
        intent.putExtra("chatRoomId", messageRoomId)
        intent.putExtra("receiverUserId", messageReceiverUserId)

        // Start the activity
        activity.startActivity(intent)
    }
    //*********************************** END GO TO CHAT SEQUENCE ***********************************

    //*********************************** GO TO POST DETAIL SEQUENCE ***********************************
    // The function to get post object based on the specified post id
    fun getPostObjectBasedOnIdAndGotoPostDetail (postId: String) {
        // Call the function to get post object of the post based on id and go to post detail
        postRepository.getPostObjectBasedOnId(postId) {postObject, foundPost ->
            if (foundPost) {
                // Call the function which will take user to the activity where the user can see post detail of the post with specified id
                gotoPostDetail(postObject)
            }
        }
    }

    // The function which will take user to the activity where the user can see post detail of the post with specified user id
    private fun gotoPostDetail (postObject: CuckooPost) {
        // Intent object
        val intent = Intent(activity, PostDetail::class.java)

        // Pass the post object to the post detail view controller
        intent.putExtra("selectedPostObject", postObject)

        // Start the activity
        activity.startActivity(intent)
    }
    //*********************************** END GO TO POST DETAIL SEQUENCE ***********************************

    //*********************************** CREATE NEW FOLLOW SEQUENCE ***********************************
    // The function to create new follow between currently logged in user and user with the specified user id
    fun createNewFollow (userId: String, followButton: Button) {
        // Call the function to create new follow between the 2 users
        followRepository.createNewFollow(userId) {buttonContent ->
            // Set content for the follow button
            followButton.text = buttonContent
        }
    }
    //*********************************** END CREATE NEW FOLLOW SEQUENCE ***********************************

    //*********************************** REMOVE FOLLOW SEQUENCE ***********************************
    // The function to remove a follow between the 2 users
    fun removeFollow (userId: String, followButton: Button) {
        // Call the function to remove a follow between the 2 users
        followRepository.removeAFollow(userId) {buttonContent ->
            // Set content for the follow button
            followButton.text = buttonContent
        }
    }
    //*********************************** END REMOVE FOLLOW SEQUENCE ***********************************

    //*********************************** CHECK FOLLOW STATUS ***********************************
    // The function to get follow status between the 2 users
    fun getFollowStatus (userId: String, followButton: Button) {
        // Call the function to get follow status between the 2 users
        followRepository.checkFollowStatus(userId) {buttonContent ->
            // Set content for the follow button
            followButton.text = buttonContent
        }
    }
    //*********************************** END CHECK FOLLOW STATUS ***********************************

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // The view object
        val view : View

        // Based on view type to return the right view holder
        return when (viewType) {
            0 -> {
                // View type 0 is for the header
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.profile_detail_header, parent, false)

                // Return the view holder
                ViewHolderProfileDetailHeader(view)
            }
            1 -> {
                // View type 1 is for the edit profile button
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.profile_detail_edit_profile, parent, false)

                // Return the view holder
                ViewHolderEditProfileButton(view)
            }
            2 -> {
                // View type 2 is for the follow/unfollow button and message button
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.profile_detail_send_message_and_follow, parent, false)

                // Return the view holder
                ViewHolderFollowMessageButton(view)
            }
            3 -> {
                // View type 3 is for the private profile row
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.profile_detail_private_profile, parent, false)

                // Return the view holder
                ViewHolderPrivateProfile(view)
            }
            else -> {
                // view type 4 is for the user album
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.profile_detail_photo_show, parent, false)

                // Return the view holder
                ViewHolderUserAlbum(view)
            }
        }
    }

    override fun getItemCount(): Int {
        // Get number of rows needed for the user album
        val numOfRowsForUserAlbum = if (arrayOfPhotos.size % 4 == 0) {
            // If there is no remainder from the division of number of elements with 4, number of rows will be
            // number of elements divided by 4
            arrayOfPhotos.size / 4
        } // Otherwise, it will be number of elements divided by 4 and add 1 into it
        else {
            (arrayOfPhotos.size / 4) + 1
        }

        // If profile is not private, show the album
        return if (!isPrivateProfile) {
            // Number of rows needed for this activity will be 2 + number of rows needed for the user album
            numOfRowsForUserAlbum + 2
        } // Otherwise, let the user know that the profile is private
        else {
            3
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // In order to prevent us from encountering the class cast exception, we need to do the following
        // Create the GSON object
        val gs = Gson()

        // First row of the RecyclerView should show the header
        if (position == 0) {
            // Call the function to set up the header row
            (holder as ViewHolderProfileDetailHeader).setUpProfileDetailHeaderPage(userObject)
        }
        // Next row will be either follow and message button or edit profile button
        // this based on if the user object is the current user or not, we need to check
        else if (position == 1) {
            // Check to see if the user object is the current user or not
            if (currentUser) {
                // If it is the current user, the row will show the edit profile button
                (holder as ViewHolderEditProfileButton).setUpEditProfileButton()
            } // Otherwise, it is other user, show the follow, message button
            else {
                (holder as ViewHolderFollowMessageButton).setUpFollowMessageButton(userObject)
            }
        }
        // For the next row, if profile is private, let user know that profile is private
        else if (position == 2 && isPrivateProfile) {
            (holder as ViewHolderPrivateProfile).setUpPrivateProfileRow()
        }
        // The rest will show the user album
        else {
            // Check to see how many images remaining in the array
            if (arrayOfPhotos.size - (position - 2) * 4 >= 4) {
                // Convert objects of the arrayOfComments array which is currently a linked tree map into a JSON string
                val jsImage1 = gs.toJson(arrayOfPhotos[(position - 2) * 4])
                val jsImage2 = gs.toJson(arrayOfPhotos[(position - 2) * 4 + 1])
                val jsImage3 = gs.toJson(arrayOfPhotos[(position - 2) * 4 + 2])
                val jsImage4 = gs.toJson(arrayOfPhotos[(position - 2) * 4 + 3])

                // Convert the JSOn string back into HBTGramPostPhoto class
                val hbtGramPostPhotoModelImage1 = gs.fromJson<PostPhoto>(jsImage1, PostPhoto::class.java)
                val hbtGramPostPhotoModelImage2 = gs.fromJson<PostPhoto>(jsImage2, PostPhoto::class.java)
                val hbtGramPostPhotoModelImage3 = gs.fromJson<PostPhoto>(jsImage3, PostPhoto::class.java)
                val hbtGramPostPhotoModelImage4 = gs.fromJson<PostPhoto>(jsImage4, PostPhoto::class.java)

                // If the remaining number of images is greater than or equal to 4, load all images into image view
                (holder as ViewHolderUserAlbum).setUpUserAlbumRow(hbtGramPostPhotoModelImage1.getImageURL(), hbtGramPostPhotoModelImage2.getImageURL(),
                    hbtGramPostPhotoModelImage3.getImageURL(), hbtGramPostPhotoModelImage4.getImageURL(),
                    hbtGramPostPhotoModelImage1.getPostId(), hbtGramPostPhotoModelImage2.getPostId(),
                    hbtGramPostPhotoModelImage3.getPostId(), hbtGramPostPhotoModelImage4.getPostId())
            } // If the remaining number of images in the array is less than 4, just load the remaining in and leave the rest blank
            else {
                // Based on the remaining number of images to decide
                when {
                    arrayOfPhotos.size - ((position - 2) * 4) == 3 -> {
                        // Convert objects of the arrayOfComments array which is currently a linked tree map into a JSON string
                        val jsImage1 = gs.toJson(arrayOfPhotos[(position - 2) * 4])
                        val jsImage2 = gs.toJson(arrayOfPhotos[(position - 2) * 4 + 1])
                        val jsImage3 = gs.toJson(arrayOfPhotos[(position - 2) * 4 + 2])

                        // Convert the JSOn string back into HBTGramPostPhoto class
                        val hbtGramPostPhotoModelImage1 = gs.fromJson<PostPhoto>(jsImage1, PostPhoto::class.java)
                        val hbtGramPostPhotoModelImage2 = gs.fromJson<PostPhoto>(jsImage2, PostPhoto::class.java)
                        val hbtGramPostPhotoModelImage3 = gs.fromJson<PostPhoto>(jsImage3, PostPhoto::class.java)

                        (holder as ViewHolderUserAlbum).setUpUserAlbumRow(hbtGramPostPhotoModelImage1.getImageURL(), hbtGramPostPhotoModelImage2.getImageURL(),
                            hbtGramPostPhotoModelImage3.getImageURL(), "",
                            hbtGramPostPhotoModelImage1.getPostId(), hbtGramPostPhotoModelImage2.getPostId(), hbtGramPostPhotoModelImage3.getPostId(), "")
                    }
                    arrayOfPhotos.size - ((position - 2) * 4) == 2 -> {
                        // Convert objects of the arrayOfComments array which is currently a linked tree map into a JSON string
                        val jsImage1 = gs.toJson(arrayOfPhotos[(position - 2) * 4])
                        val jsImage2 = gs.toJson(arrayOfPhotos[(position - 2) * 4 + 1])

                        // Convert the JSOn string back into HBTGramPostPhoto class
                        val hbtGramPostPhotoModelImage1 = gs.fromJson<PostPhoto>(jsImage1, PostPhoto::class.java)
                        val hbtGramPostPhotoModelImage2 = gs.fromJson<PostPhoto>(jsImage2, PostPhoto::class.java)

                        (holder as ViewHolderUserAlbum).setUpUserAlbumRow(hbtGramPostPhotoModelImage1.getImageURL(), hbtGramPostPhotoModelImage2.getImageURL(), "", "",
                            hbtGramPostPhotoModelImage1.getPostId(), hbtGramPostPhotoModelImage2.getPostId(), "", "")
                    }
                    arrayOfPhotos.size - ((position - 2) * 4) == 1 -> {
                        // Convert objects of the arrayOfComments array which is currently a linked tree map into a JSON string
                        val jsImage1 = gs.toJson(arrayOfPhotos[(position - 2) * 4])

                        // Convert the JSOn string back into HBTGramPostPhoto class
                        val hbtGramPostPhotoModelImage1 = gs.fromJson<PostPhoto>(jsImage1, PostPhoto::class.java)

                        (holder as ViewHolderUserAlbum).setUpUserAlbumRow(hbtGramPostPhotoModelImage1.getImageURL(), "", "", "",
                            hbtGramPostPhotoModelImage1.getPostId(), "", "", "")
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            // First row of the RecyclerView should show the header
            0
        } else if (position == 1) {
            // Next row will be either follow and message button or edit profile button
            // this based on if the user object is the current user or not, we need to check
            // Check to see if the user object is the current user or not
            if (currentUser) {
                // If it is the current user, the row will show the edit profile button
                1
            } // Otherwise, it is other user, show the follow, message button
            else {
                2
            }
        } // For the next row, if profile is private, let user know that profile is private
        else if (position == 2 && isPrivateProfile) {
            3
        }
        // The rest will show the user album
        else {
            4
        }
    }
}