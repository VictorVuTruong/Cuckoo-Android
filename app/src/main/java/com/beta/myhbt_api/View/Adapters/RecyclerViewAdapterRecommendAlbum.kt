package com.beta.myhbt_api.View.Adapters

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.beta.myhbt_api.Controller.GetPostBasedOnIdService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Model.HBTGramPost
import com.beta.myhbt_api.Model.HBTGramPostPhoto
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Fragments.RecommendAlbumFragment
import com.beta.myhbt_api.View.HBTGramPostDetail
import com.bumptech.glide.Glide
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecyclerViewAdapterRecommendAlbum (arrayOfPhotos: ArrayList<HBTGramPostPhoto>, activity: Activity, recommendAlbumFragment: RecommendAlbumFragment): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    // Array of photos recommended for the user
    private val arrayOfPhotos = arrayOfPhotos

    // Activity of the parent
    private val activity = activity

    // The parent fragment
    private val recommendAlbumFragment = recommendAlbumFragment

    //*********************************** VIEW HOLDERS FOR THE RECYCLER VIEW ***********************************
    // ViewHolder for the recommend album header
    inner class ViewHolderRecommendAlbumHeader internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // The function to set up the row
        fun setUpHeaderRow () {}
    }

    // ViewHolder for the recommend album row
    inner class ViewHolderRecommendAlbumRow internal constructor(itemView: View) :
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

    // ViewHolder for the recommend album row
    inner class ViewHolderRecommendAlbumLoadMoreRow internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        // Component from the layout
        private val loadMoreLayout: ConstraintLayout = itemView.findViewById(R.id.loadMorePostLayout)

        // The function to set up the load more row
        fun setUpLoadMoreRow () {
            // Set up on click listener for the load more layout
            loadMoreLayout.setOnClickListener {
                // Call the function to load more post photos
                recommendAlbumFragment.getRecommendedPhotos()
            }
        }
    }

    //*********************************** END VIEW HOLDERS FOR THE RECYCLER VIEW ***********************************

    //*********************************** GO TO POST DETAIL SEQUENCE ***********************************
    /*
    In this sequence, we will do 2 things
    1. Get post object based on the specified id
    2. Go to the post detail activity
     */

    // The function to get post object based on the specified post id
    fun getPostObjectBasedOnIdAndGotoPostDetail (postId: String) {
        // If the post detail is empty, get out of the sequence
        if (postId == "") {
            return
        }

        // Create the get post based on id service
        val getPostBasedOnIdService: GetPostBasedOnIdService = RetrofitClientInstance.getRetrofitInstance(activity)!!.create(
            GetPostBasedOnIdService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getPostBasedOnIdService.getPostBasedOnId(postId)

        // Perform the call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                print("There seem be be an error ${t.stackTrace}")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is not empty it means that the token is valid
                if (response.body() != null) {
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get data from the response
                    val data = (((responseBody["data"] as Map<String, Any>)["documents"]) as ArrayList<Map<String, Any>>)[0]

                    // In order to prevent us from encountering the class cast exception, we need to do the following
                    // Create the GSON object
                    val gs = Gson()

                    // Convert a linked tree map into a JSON string
                    val jsPost = gs.toJson(data)

                    // Convert the JSOn string back into HBTGramPost class
                    val hbtGramPostModel = gs.fromJson<HBTGramPost>(jsPost, HBTGramPost::class.java)

                    // Call the function which will take user to the activity where the user can see post detail of the post with specified id
                    gotoPostDetail(hbtGramPostModel)
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function which will take user to the activity where the user can see post detail of the post with specified user id
    fun gotoPostDetail (postObject: HBTGramPost) {
        // Intent object
        val intent = Intent(activity, HBTGramPostDetail::class.java)

        // Pass the post object to the post detail view controller
        intent.putExtra("selectedPostObject", postObject)

        // Start the activity
        activity.startActivity(intent)
    }
    //*********************************** END GO TO POST DETAIL SEQUENCE ***********************************

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // The view object
        val view : View

        // Based on view type to return the right view holder
        return when (viewType) {
            0 -> {
                // View type 0 is for the header
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recommend_photo_header, parent, false)

                // Return the view holder
                ViewHolderRecommendAlbumHeader(view)
            } 1 -> {
                // view type 1 is for the user album
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.profile_detail_photo_show, parent, false)

                // Return the view holder
                ViewHolderRecommendAlbumRow(view)
            } else -> {
                // View type 2 is for the load more row
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.hbt_gram_post_item_load_more, parent, false)

                // Return the view holder
                ViewHolderRecommendAlbumLoadMoreRow(view)
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

        // Number of rows needed for this activity will be 2 + number of rows needed for the user album
        return numOfRowsForUserAlbum + 2
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Get number of rows needed for the album
        val numOfRowsForAlbum = if (arrayOfPhotos.size % 4 == 0) {
            // If there is no remainder from the division of number of elements with 4, number of rows will be
            // number of elements divided by 4
            arrayOfPhotos.size / 4
        } // Otherwise, it will be number of elements divided by 4 and add 1 into it
        else {
            (arrayOfPhotos.size / 4) + 1
        }

        // In order to prevent us from encountering the class cast exception, we need to do the following
        // Create the GSON object
        val gs = Gson()

        // First row of the RecyclerView should show the header
        if (position == 0) {
            // Call the function to set up the header row
            (holder as ViewHolderRecommendAlbumHeader).setUpHeaderRow()
        } // The rest will show the album
        else if (position in 1..numOfRowsForAlbum) {
            // Check to see how many images remaining in the array
            if (arrayOfPhotos.size - (position - 1) * 4 >= 4) {
                // Convert objects of the arrayOfComments array which is currently a linked tree map into a JSON string
                val jsImage1 = gs.toJson(arrayOfPhotos[(position - 1) * 4])
                val jsImage2 = gs.toJson(arrayOfPhotos[(position - 1) * 4 + 1])
                val jsImage3 = gs.toJson(arrayOfPhotos[(position - 1) * 4 + 2])
                val jsImage4 = gs.toJson(arrayOfPhotos[(position - 1) * 4 + 3])

                // Convert the JSOn string back into HBTGramPostPhoto class
                val hbtGramPostPhotoModelImage1 = gs.fromJson<HBTGramPostPhoto>(jsImage1, HBTGramPostPhoto::class.java)
                val hbtGramPostPhotoModelImage2 = gs.fromJson<HBTGramPostPhoto>(jsImage2, HBTGramPostPhoto::class.java)
                val hbtGramPostPhotoModelImage3 = gs.fromJson<HBTGramPostPhoto>(jsImage3, HBTGramPostPhoto::class.java)
                val hbtGramPostPhotoModelImage4 = gs.fromJson<HBTGramPostPhoto>(jsImage4, HBTGramPostPhoto::class.java)

                // If the remaining number of images is greater than or equal to 4, load all images into image view
                (holder as ViewHolderRecommendAlbumRow).setUpUserAlbumRow(hbtGramPostPhotoModelImage1.getImageURL(), hbtGramPostPhotoModelImage2.getImageURL(),
                    hbtGramPostPhotoModelImage3.getImageURL(), hbtGramPostPhotoModelImage4.getImageURL(),
                    hbtGramPostPhotoModelImage1.getPostId(), hbtGramPostPhotoModelImage2.getPostId(),
                    hbtGramPostPhotoModelImage3.getPostId(), hbtGramPostPhotoModelImage4.getPostId())
            } // If the remaining number of images in the array is less than 4, just load the remaining in and leave the rest blank
            else {
                // Based on the remaining number of images to decide
                when {
                    arrayOfPhotos.size - ((position - 1) * 4) == 3 -> {
                        // Convert objects of the arrayOfComments array which is currently a linked tree map into a JSON string
                        val jsImage1 = gs.toJson(arrayOfPhotos[(position - 1) * 4])
                        val jsImage2 = gs.toJson(arrayOfPhotos[(position - 1) * 4 + 1])
                        val jsImage3 = gs.toJson(arrayOfPhotos[(position - 1) * 4 + 2])

                        // Convert the JSOn string back into HBTGramPostPhoto class
                        val hbtGramPostPhotoModelImage1 = gs.fromJson<HBTGramPostPhoto>(jsImage1, HBTGramPostPhoto::class.java)
                        val hbtGramPostPhotoModelImage2 = gs.fromJson<HBTGramPostPhoto>(jsImage2, HBTGramPostPhoto::class.java)
                        val hbtGramPostPhotoModelImage3 = gs.fromJson<HBTGramPostPhoto>(jsImage3, HBTGramPostPhoto::class.java)

                        (holder as ViewHolderRecommendAlbumRow).setUpUserAlbumRow(hbtGramPostPhotoModelImage1.getImageURL(), hbtGramPostPhotoModelImage2.getImageURL(),
                            hbtGramPostPhotoModelImage3.getImageURL(), "",
                            hbtGramPostPhotoModelImage1.getPostId(), hbtGramPostPhotoModelImage2.getPostId(), hbtGramPostPhotoModelImage3.getPostId(), "")
                    }
                    arrayOfPhotos.size - ((position - 1) * 4) == 2 -> {
                        // Convert objects of the arrayOfComments array which is currently a linked tree map into a JSON string
                        val jsImage1 = gs.toJson(arrayOfPhotos[(position - 1) * 4])
                        val jsImage2 = gs.toJson(arrayOfPhotos[(position - 1) * 4 + 1])

                        // Convert the JSOn string back into HBTGramPostPhoto class
                        val hbtGramPostPhotoModelImage1 = gs.fromJson<HBTGramPostPhoto>(jsImage1, HBTGramPostPhoto::class.java)
                        val hbtGramPostPhotoModelImage2 = gs.fromJson<HBTGramPostPhoto>(jsImage2, HBTGramPostPhoto::class.java)

                        (holder as ViewHolderRecommendAlbumRow).setUpUserAlbumRow(hbtGramPostPhotoModelImage1.getImageURL(), hbtGramPostPhotoModelImage2.getImageURL(), "", "",
                            hbtGramPostPhotoModelImage1.getPostId(), hbtGramPostPhotoModelImage2.getPostId(), "", "")
                    }
                    arrayOfPhotos.size - ((position - 1) * 4) == 1 -> {
                        // Convert objects of the arrayOfComments array which is currently a linked tree map into a JSON string
                        val jsImage1 = gs.toJson(arrayOfPhotos[(position - 1) * 4])

                        // Convert the JSOn string back into HBTGramPostPhoto class
                        val hbtGramPostPhotoModelImage1 = gs.fromJson<HBTGramPostPhoto>(jsImage1, HBTGramPostPhoto::class.java)

                        (holder as ViewHolderRecommendAlbumRow).setUpUserAlbumRow(hbtGramPostPhotoModelImage1.getImageURL(), "", "", "",
                            hbtGramPostPhotoModelImage1.getPostId(), "", "", "")
                    }
                }
            }
        } else {
            // Call the function to set up the load more row
            (holder as ViewHolderRecommendAlbumLoadMoreRow).setUpLoadMoreRow()
        }
    }

    override fun getItemViewType(position: Int): Int {
        // Get number of rows needed for the album
        val numOfRowsForAlbum = if (arrayOfPhotos.size % 4 == 0) {
            // If there is no remainder from the division of number of elements with 4, number of rows will be
            // number of elements divided by 4
            arrayOfPhotos.size / 4
        } // Otherwise, it will be number of elements divided by 4 and add 1 into it
        else {
            (arrayOfPhotos.size / 4) + 1
        }

        return when (position) {
            0 -> {
                // First row of the RecyclerView should show the header
                0
            } // The rest will show the album
            in 1..numOfRowsForAlbum -> {
                1
            }
            else -> {
                2
            }
        }
    }
}