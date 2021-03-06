package com.beta.cuckoo.View.UserStats

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.beta.cuckoo.Model.UserCommentInteraction
import com.beta.cuckoo.Model.UserInteraction
import com.beta.cuckoo.Model.UserLikeInteraction
import com.beta.cuckoo.Model.UserProfileVisit
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.beta.cuckoo.View.Adapters.RecyclerViewAdapterUserStatsDetail
import com.beta.cuckoo.ViewModel.UserStatsViewModel
import kotlinx.android.synthetic.main.activity_user_stats_detail.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class UserStatsDetail : AppCompatActivity() {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // User stats view model
    private lateinit var userStatsViewModel: UserStatsViewModel

    // User repository
    private lateinit var userRepository: UserRepository

    // Adapter for the RecyclerView
    private var adapter: RecyclerViewAdapterUserStatsDetail ?= null

    // The variable which will keep track of which kind of user stats status to be shown at this activity
    private var userStatsKindToShow = ""

    // Array of user interaction status objects to be shown to the user (if the activity suppose to show it)
    private var arrayOfUserInteraction = ArrayList<UserInteraction>()

    // Array of user like interaction status objects to be shown to the user
    private var arrayOfUserLikeInteraction = ArrayList<UserLikeInteraction>()

    // Array of user comment interaction status objects to be shown to the user
    private var arrayOfUserCommentInteraction = ArrayList<UserCommentInteraction>()

    // Array of user profile visit objects to be shown to the user
    private var arrayOfUserProfileVisit = ArrayList<UserProfileVisit>()

    override fun onBackPressed() {
        super.onBackPressed()
        this.finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_stats_detail)

        // Hide the action bar
        supportActionBar!!.hide()

        // Instantiate user repository
        userRepository = UserRepository(executorService, applicationContext)

        // Instantiate the user stats view model
        userStatsViewModel = UserStatsViewModel(applicationContext)

        // Set on click listener for the back button
        backButtonUserStatsDetail.setOnClickListener {
            this.finish()
        }

        // Get which kind of user stats detail to load from previous activity
        userStatsKindToShow = intent.getStringExtra("userStatsKindToShow")!!

        // Instantiate the recycler view
        userStatsDetailView.layoutManager = LinearLayoutManager(applicationContext)
        userStatsDetailView.itemAnimator = DefaultItemAnimator()

        // Call the function to get detail user stats of the currently logged in user
        getUserStats()
    }

    //**************************** GET INFO OF CURRENT USER SEQUENCE ****************************
    // The function to call the right function to get the right user stats
    private fun getUserStats () {
        // Based on which kind user stats info to load to load to right thing
        when (userStatsKindToShow) {
            "userInteraction" -> {
                // Call the function to load user interaction
                getUserInteraction()
            }
            "userLikeInteraction" -> {
                getUserLikeInteraction()
            }
            "userCommentInteraction" -> {
                getUserCommentInteraction()
            }
            "userProfileVisit" -> {
                getUserProfileVisit()
            }
        }
    }

    // The function to get list of user interaction for the currently logged in user
    private fun getUserInteraction () {
        // Call the function to get detail user interaction of the currently logged in user
        userStatsViewModel.getListOfUserInteraction { arrayOfUserInteractionParam ->
            // Update the array of user interaction
            arrayOfUserInteraction = arrayOfUserInteractionParam

            // Update the adapter
            adapter = RecyclerViewAdapterUserStatsDetail(arrayOfUserInteraction, arrayOfUserLikeInteraction, arrayOfUserCommentInteraction, arrayOfUserProfileVisit, this@UserStatsDetail, userRepository)

            // Add adapter to the RecyclerView
            userStatsDetailView.adapter = adapter
        }
    }

    // The function to get list of user like interaction for the currently logged in user
    private fun getUserLikeInteraction () {
        // Call the function to get detail user like interaction of the currently logged in use
        userStatsViewModel.getListOfUserLikeInteraction { arrayOfUserLikeInteractionParam ->
            // Update the array of user like interaction
            arrayOfUserLikeInteraction = arrayOfUserLikeInteractionParam

            // Update the adapter
            adapter = RecyclerViewAdapterUserStatsDetail(arrayOfUserInteraction, arrayOfUserLikeInteraction, arrayOfUserCommentInteraction, arrayOfUserProfileVisit, this@UserStatsDetail, userRepository)

            // Add adapter to the RecyclerView
            userStatsDetailView.adapter = adapter
        }
    }

    // The function to get list of user comment interaction for the currently logged in user
    private fun getUserCommentInteraction () {
        // Call the function to get detail user comment interaction of the currently logged in user
        userStatsViewModel.getListOfUserCommentInteraction { arrayOfUserCommentInteractionParam ->
            // Update the array of user comment interaction
            arrayOfUserCommentInteraction = arrayOfUserCommentInteractionParam

            // Update the adapter
            adapter = RecyclerViewAdapterUserStatsDetail(arrayOfUserInteraction, arrayOfUserLikeInteraction, arrayOfUserCommentInteraction, arrayOfUserProfileVisit, this@UserStatsDetail, userRepository)

            // Add adapter to the RecyclerView
            userStatsDetailView.adapter = adapter
        }
    }

    // The function to get list of user profile visits for the currently logged in user
    private fun getUserProfileVisit () {
        // Call the function to get detail user profile visit of the currently logged in user
        userStatsViewModel.getListOfUserProfileVisit { arrayOfUserProfileVisitParam ->
            // Update the array of user comment interaction
            arrayOfUserProfileVisit = arrayOfUserProfileVisitParam

            // Update the adapter
            adapter = RecyclerViewAdapterUserStatsDetail(arrayOfUserInteraction, arrayOfUserLikeInteraction, arrayOfUserCommentInteraction, arrayOfUserProfileVisit, this@UserStatsDetail, userRepository)

            // Add adapter to the RecyclerView
            userStatsDetailView.adapter = adapter
        }
    }
    //**************************** END GET USER DETAIL STATS INFO SEQUENCE ****************************
}
