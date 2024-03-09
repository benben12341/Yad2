package com.example.yad2.viewModels

import com.example.yad2.models.Model
import com.example.yad2.models.User
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class MainViewModel @RequiresApi(api = Build.VERSION_CODES.N) constructor() : ViewModel() {
    var data: LiveData<User>

    init {
        data = Model.instance.loggedUser
    }

    fun getUserData(): LiveData<User> {
        return data
    }
}