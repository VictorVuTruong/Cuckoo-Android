package com.beta.myhbt_api.View

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.myhbt_api.Controller.GetCurrentlyLoggedInUserInfoService
import com.beta.myhbt_api.Controller.GetPhotosOfUserService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.Model.HBTGramPostPhoto
import com.beta.myhbt_api.Model.User
import com.beta.myhbt_api.R
import com.beta.myhbt_api.View.Adapters.RecyclerViewAdapterProfileDetail
import kotlinx.android.synthetic.main.activity_profile_detail.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Array

class ProfileDetail : AppCompatActivity() {
    // Adapter for the RecyclerView
    private lateinit var adapter: RecyclerViewAdapterProfileDetail

    // User object of the user
    private var userObject = User("", "", "", "", "", "", "", "","", "", "", "", "", "", "")

    // Array of images
    private var arrayOfImages = ArrayList<HBTGramPostPhoto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_detail)

        // Get selected user object from the previous activity
        userObject = intent.getSerializableExtra("selectedUserObject") as User

        // Instantiate the recycler view
        profileDetailView.layoutManager = LinearLayoutManager(applicationContext)
        profileDetailView.itemAnimator = DefaultItemAnimator()

        // Call the function to load further info of the user
        getInfoOfCurrentUserAndFurtherInfo()
    }

    //******************************************* LOAD INFO OF USER SEQUENCE *******************************************
    // The function to get id of current user which will then check if user at this activity is current or not
    private fun getInfoOfCurrentUserAndFurtherInfo () {
        // Create the get current user info service
        val getCurrentlyLoggedInUserInfoService: GetCurrentlyLoggedInUserInfoService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
            GetCurrentlyLoggedInUserInfoService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getCurrentlyLoggedInUserInfoService.getCurrentUserInfo()

        // Perform the call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is not empty it means that the token is valid
                if (response.body() != null) {
                    val body = response.body()
                    print(body)
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get data from the response body
                    val data = responseBody["data"] as Map<String, Any>

                    // Get user id in the database of the currently logged in user
                    val userId = data["_id"] as String

                    // Check to see if user object at this activity is the current user or not, then call
                    // the function to set up rest of the view
                    if (userId == userObject.getId()) {
                        // Call the function to set up the rest as well as let the function know that user at this activity is the current user
                        loadPhotosOfUser(userObject.getId(), true)
                    } // Otherwise, call the function to set up the rest and let it know that user at this activity is not the current user
                    else {
                        loadPhotosOfUser(userObject.getId(), false)
                    }
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    // The function to load all photos created by the user
    fun loadPhotosOfUser (userId: String, currentUser: Boolean) {
        // Create the get images of user service
        val getPhotosOfUserService: GetPhotosOfUserService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
            GetPhotosOfUserService::class.java)

        // Create the call object in order to perform the call
        val call: Call<Any> = getPhotosOfUserService.getPhotosOfUser(userId)

        // Perform the call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is not empty it means that the token is valid
                if (response.body() != null) {
                    val body = response.body()
                    print(body)
                    // Body of the request
                    val responseBody = response.body() as Map<String, Any>

                    // Get data from the response body (array of images created by the user)
                    val data = responseBody["data"] as ArrayList<HBTGramPostPhoto>

                    // Set the array of images be the one we just got
                    arrayOfImages = data

                    // Update the adapter
                    adapter = RecyclerViewAdapterProfileDetail(arrayOfImages, userObject, this@ProfileDetail, currentUser)

                    // Add adapter to the RecyclerView
                    profileDetailView.adapter = adapter
                } else {
                    print("Something is not right")
                }
            }
        })
    }

    //******************************************* END LOAD INFO OF USER SEQUENCE *******************************************
}