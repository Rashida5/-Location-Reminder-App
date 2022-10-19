package com.udacity.project4.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
//State of User Login or Log out
enum class State{
    AUTH,
    UNAUTH
}
class FirebaseViewModel: ViewModel() {
    val AuthState=FirebaseUserLiveData().map{ user->
        if(user!=null){
            State.AUTH}
        else{
            State.UNAUTH
        }
    }
}