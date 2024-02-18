package com.example.yad2.viewModels

import Model
import User
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class MainViewModel @RequiresApi(api = Build.VERSION_CODES.N) constructor() : ViewModel() {
    var data: LiveData<User?>

    init {
        data = Model.instance.getLoggedUser()
    }

    fun getData(): LiveData<User?> {
        return data
    }
}