package com.beta.cuckoo.View.WelcomeView

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import com.beta.cuckoo.R
import com.beta.cuckoo.Repository.UserRepositories.UserRepository
import com.beta.cuckoo.View.Authentication.ForgotPassword
import com.beta.cuckoo.View.MainMenu.MainMenu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LoginActivity : AppCompatActivity() {
    // Instance of the FirebaseAuth
    private val mAuth = FirebaseAuth.getInstance()

    override fun onBackPressed() {
        super.onBackPressed()

        // Start the main activity again (welcome page)
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)

        // Finish the current activity
        this.finish()

        overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Hide the action bar
        supportActionBar!!.hide()

        // Add on click listener to the back button
        backButtonLogin.setOnClickListener{
            // Start the main activity again (welcome page)
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)

            // Finish the current activity
            this.finish()
            overridePendingTransition(R.animator.slide_in_left, R.animator.slide_out_right)
        }

        // Set up event listener for the login button
        submitLoginButton.setOnClickListener {
            // Call the function to perform the login operation
            login(emailTextField.text.toString(), passwordTextField.text.toString())
        }

        // Set up on click listener for the forgot password button
        forgotPasswordButton.setOnClickListener {
            // Start the forgot password activity
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
        }
    }

    // The function to perform the login procedure
    private fun login (email: String, password: String) {
        // Get the shared preference (memory) instance
        val memory = PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Get Firebase Auth token of the user
                    val mUser: FirebaseUser? = mAuth.currentUser
                    mUser!!.getIdToken(true)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Get id token
                                val idToken = task.result.token

                                // Save id token to the memory
                                memory.putString("idToken", idToken)
                                memory.apply()

                                // Sign in success, update UI with the signed-in user's information
                                val intent = Intent(applicationContext, MainMenu::class.java)
                                startActivity(intent)

                                // Finish this activity
                                this@LoginActivity.finish()
                            } else {
                                // Handle error -> task.getException();
                            }
                        }
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(applicationContext, "Password or email is not right", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
