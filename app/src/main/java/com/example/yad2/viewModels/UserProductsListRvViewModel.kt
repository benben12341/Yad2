package com.example.yad2.viewModels

import com.example.yad2.models.Model
import com.example.yad2.models.Product
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class UserProductsListRvViewModel @RequiresApi(api = Build.VERSION_CODES.N) constructor() :
    ViewModel() {
    public var data: LiveData<List<Product>> = Model.instance.getProductOfUser()

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun refreshUserItems() {
        Model.instance.refreshProductsByMyUser()
    }
}