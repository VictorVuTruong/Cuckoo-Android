package com.beta.myhbt_api.View

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.beta.myhbt_api.Controller.LoginPostDataService
import com.beta.myhbt_api.Controller.RetrofitClientInstance
import com.beta.myhbt_api.R
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Set up event listener for the login button
        submitLoginButton.setOnClickListener {
            // Call the function to perform the login operation
            login(emailTextField.text.toString(), passwordTextField.text.toString())
        }
    }

    // The function to perform the login procedure
    private fun login (email: String, password: String) {
        // Create the post service
        val postService: LoginPostDataService = RetrofitClientInstance.getRetrofitInstance(applicationContext)!!.create(
            LoginPostDataService::class.java)

        // The call object which will then be used to perform the API call
        val call: Call<Any> = postService.login(email, password)

        // Perform the API call
        call.enqueue(object: Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                // Report the error if something is not right
                print("Boom")
            }

            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                // If the response body is null, it means that the user may didn't enter the correct email or password
                if (response.body() == null) {
                    // Show the user that the login was not successful
                    Toast.makeText(applicationContext, "Password or email is not right", Toast.LENGTH_SHORT).show()
                } else {
                    // Go to the mail page activity
                    val intent = Intent(applicationContext, MainPage::class.java)
                    startActivity(intent)
                }
            }
        })
    }
}
