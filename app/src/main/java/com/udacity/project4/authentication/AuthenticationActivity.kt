package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    lateinit var binding:ActivityAuthenticationBinding
    lateinit var btn: Button
   val firebaseviewmodel:FirebaseViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    //   setContentView(R.layout.activity_authentication)
       firebaseviewmodel.AuthState.observe(this , {
            Log.i("msg","UNAUTH")
            if(it==State.AUTH){
                Log.i("msg","I solve1")
                val IntentToReminder= Intent(this ,RemindersActivity::class.java)
                startActivity(IntentToReminder)
                finish()
            }
            else if(it==State.UNAUTH){
                Log.i("msg","I solve2")
                binding= DataBindingUtil.setContentView(this,R.layout.activity_authentication)
                binding.login.setOnClickListener {
                    launchSignInFlow()
                }
            } else{
                Log.i("msg","I solve3")
                binding= DataBindingUtil.setContentView(this,R.layout.activity_authentication)
                btn=findViewById(R.id.login)
                btn.setOnClickListener {
                    launchSignInFlow()
                }
            }
        })
     /*   btn=findViewById(R.id.login)
        btn.setOnClickListener { launchSignInFlow() }*/
//         TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google

//          TODO: If the user was authenticated, send him to RemindersActivity

//          TODO: a bonus is to customize the sign in flow to look nice using :

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i("msg","I solve")
        if(requestCode== SIGN_IN_REQUEST_CODE){
            val response=IdpResponse.fromResultIntent(data)

            if(resultCode==Activity.RESULT_OK){
                Log.i("msg","Successfully signed in user ${FirebaseAuth.getInstance().currentUser}")
               val intenttoreminder=Intent(this, RemindersActivity::class.java)
                startActivity(intenttoreminder)
            } else{
                //failed to sign in
                Log.i("msg","Un successfull${response?.error?.errorCode}")

                return
            }
        }
    }
    fun launchSignInFlow(){
        Log.i("msg","launch Sign In flow")
        val providors= arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )
        startActivityForResult(AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providors)
            .build(),
            SIGN_IN_REQUEST_CODE

        )
    }

    companion object{
        const val SIGN_IN_REQUEST_CODE=111
    }
}
