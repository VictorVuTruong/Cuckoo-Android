package com.beta.cuckoo.View.MainMenu

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Gravity
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.beta.cuckoo.BackgroundServices
import com.beta.cuckoo.Network.User.GetCurrentlyLoggedInUserInfoService
import com.beta.cuckoo.Network.User.LogoutPostDataService
import com.beta.cuckoo.Network.RetrofitClientInstance
import com.beta.cuckoo.FirebaseMessagingService
import com.beta.cuckoo.Model.User
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.NotificationRepositories.NotificationRepository
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.beta.cuckoo.View.Fragments.*
import com.beta.cuckoo.View.UserInfoView.ProfileDetail
import com.beta.cuckoo.View.WelcomeView.MainActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.android.synthetic.main.activity_main_menu.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainMenu : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    // Executor service to perform works in the background
    private val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    // The user repository
    private lateinit var userRepository: UserRepository

    // The notification repository
    private lateinit var notificationRepository: NotificationRepository

    // These objects are used for socket.io
    private val gson = Gson()

    // Instance of the FirebaseAuth
    private val mAuth = FirebaseAuth.getInstance()

    // User object of the currently logged in user
    private lateinit var currentUserObject: User

    companion object {
        lateinit var mSocket: Socket
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        // Instantiate user repository
        userRepository = UserRepository(executorService, applicationContext)

        // Instantiate notification repository
        notificationRepository = NotificationRepository(executorService, applicationContext)

        // Set this thing up for the button which will be used to open the hamburger menu
        setSupportActionBar(toolbar)
        nav_view.setNavigationItemSelectedListener(this)

        // Hide the action bar
        supportActionBar!!.hide()

        // To open the hamburger menu
        val toggle = ActionBarDrawerToggle(
            this, drawer_layout,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )

        // Set the toggle so that user can open and close this menu
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        // Set the icon for the button
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)// set drawable icon
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Check the previous activity to see what it is. If the previous activity is the profile detail page,
        // load the profile page instead of the dashboard
        if (intent.getStringExtra("previousActivityName") == "profileDetailPage") {
            // Hide the action bar
            supportActionBar!!.hide()

            // Load the profile page
            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    ProfileFragment()
                ).commit()
                nav_view.setCheckedItem(R.id.nav_dashboard)
                supportActionBar!!.title = "Profile Settings"
            }
        } // Otherwise, load the dashboard
        else {
            // Set the first fragment to display to be the dashboard
            if (savedInstanceState == null) {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    DashboardFragment()
                ).commit()
                nav_view.setCheckedItem(R.id.nav_dashboard)
                supportActionBar!!.title = "Home page"
            }
        }

        // Call the function to do initial set up
        setUp()
    }

    // The function to set up
    private fun setUp () {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notification"
            val descriptionText = "Channel for the notification"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("notification_channel", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Call the function to load info of the currently logged in user
        getCurrentUserInfo()
    }

    //************************ DO THINGS WITH THE SOCKET.IO ************************
    // The function to set up socket.IO
    private fun setUpSocketIO () {
        // This address is to connect with the server
        //mSocket = IO.socket("http://10.0.2.2:3000")
        mSocket = IO.socket("https://myhbt-api.herokuapp.com")
        //mSocket = IO.socket("http://localhost:3000")

        // Connect to the socket
        mSocket.connect()

        // Get registration token of the user and let user join in the notification room
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {task ->
            // Registration token of the user
            val token = task.token

            // Call the function to get user id of the currently logged in user
            userRepository.getInfoOfCurrentUser { userObject ->
                // Call the function to update socket id of the currently logged in user
                notificationRepository.updateNotificationSocket(userObject.getId(), token) {
                    // Start Firebase Messaging service
                    val intentService = Intent(this, FirebaseMessagingService::class.java)
                    startService(intentService)
                }
            }
        }
    }
    //************************ END WORKING WITH SOCKET.IO ************************

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawer_layout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_dashboard -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    DashboardFragment()
                ).commit()
                supportActionBar!!.title = "Dashboard"
            }
            R.id.nav_chat -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    ChatFragment()
                ).commit()
                supportActionBar!!.title = "Chat"
            }
            R.id.nav_create_post -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    CreatePostFragment()
                ).commit()
                supportActionBar!!.title = "Create new post"
            }
            R.id.nav_profile -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    ProfileFragment()
                ).commit()
                supportActionBar!!.title = "Profile Settings"
            }
            R.id.nav_search_friend -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    SearchFriendFragment()
                ).commit()
                supportActionBar!!.title = "Search friends"
            }
            R.id.nav_personal_profile_page -> {
                // Go to the activity where user can see profile detail of user's own
                // The intent object
                val intent = Intent(this, ProfileDetail::class.java)

                // Update user object property of the profile detail activity
                intent.putExtra("selectedUserObject", currentUserObject)

                // Start the activity
                startActivity(intent)
            }
            R.id.nav_update_location -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    UpdateLocationFragment()
                ).commit()
                supportActionBar!!.title = "Locations"
            }
            R.id.nav_user_stats -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    UserStatsFragment()
                ).commit()
                supportActionBar!!.title = "Activity summary"
            }
            R.id.nav_recommend_album -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    RecommendAlbumFragment()
                ).commit()
                supportActionBar!!.title = "Explore"
            }
            R.id.nav_list_of_users_around -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    SearchUserAroundFragment()
                ).commit()
                supportActionBar!!.title = "Who's around?"
            }
            R.id.nav_list_of_posts_around -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    PostsAroundFragment()
                ).commit()
                supportActionBar!!.title = "What's going on around?"
            }
            R.id.nav_notifications -> {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container,
                    NotificationFragment()
                ).commit()
                supportActionBar!!.title = "Notifications"
            }
            R.id.nav_signout -> {
                // Call the function to sign user out
                signOut()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    // The function to sign user out
    private fun signOut () {
        // Call the function to start signing user out
        userRepository.getInfoOfCurrentUser {userObject ->
            // Call the function to get info of the currently logged in user
            userRepository.signOut {
                // Get registration token of the user delete notification socket as user signs out
                FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {task ->
                    // Registration token of the user
                    val token = task.token

                    // Call the function to delete notification socket for the user
                    notificationRepository.deleteNotificationSocket(userObject.getId(), token) {
                        // Sign the user out with FirebaseAuth
                        mAuth.signOut()

                        // Go to the main page activity
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)

                        // Finish this activity
                        this@MainMenu.finish()
                    }
                }
            }
        }
    }

    // The function to get user info based on id
    fun getCurrentUserInfo() {
        // Call the function to get info of the currently logged in user
        userRepository.getInfoOfCurrentUser { userObject ->
            /*
                    // Load full name into the TextView
                    userFullNameDrawerMenu.text = userObject.getFullName()

                    // Load email into the TextView
                    userEmailDrawerMenu.text = userObject.getEmail()

                    // Load avatar into the ImageView
                    Glide.with(applicationContext)
                        .load(userObject.getAvatarURL())
                        .into(userAvatarDrawerMenu)

                     */

            // Update current user object for this activity
            currentUserObject = userObject

            // Call the function to set up socket io for the whole app
            setUpSocketIO()
        }
    }

    fun openDrawerMenu () {
        drawer_layout.openDrawer(Gravity.LEFT)
    }
}