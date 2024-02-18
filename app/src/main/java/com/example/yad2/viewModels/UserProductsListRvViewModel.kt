package com.example.yad2.viewModels

import Model
import Product
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class UserProductsListRvViewModel @RequiresApi(api = Build.VERSION_CODES.N) constructor() :
    ViewModel() {
    private var data: LiveData<List<Product>> = Model.instance.getProductOfUser()

    fun getData(): LiveData<List<Product>> {
        return this.data
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun refreshUserItems() {
        Model.instance.refreshProductsByMyUser()
    }
}