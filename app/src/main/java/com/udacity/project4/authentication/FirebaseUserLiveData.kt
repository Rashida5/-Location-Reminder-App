package com.udacity.project4.authentication

import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class FirebaseUserLiveData : LiveData<FirebaseUser>() {

    private val mAuth=FirebaseAuth.getInstance()
    private val firebaseListener=FirebaseAuth.AuthStateListener {
        value=it.currentUser
    }

    override fun onActive() {
        mAuth.addAuthStateListener(firebaseListener)
    }

    override fun onInactive() {
        mAuth.removeAuthStateListener(firebaseListener)
    }
}