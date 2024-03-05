package com.example.yad2.viewModels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.yad2.models.Model
import com.example.yad2.models.Product

class FavoriteProductListRvViewModel @RequiresApi(api = Build.VERSION_CODES.N) constructor() :
    ViewModel() {
    var data: LiveData<List<Product>>

    init {
        data = Model.instance.getAllFavoriteProductsByUser()
    }

    fun getData(): LiveData<List<Product>> {
        return data
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun refreshFavoriteItems() {
        Model.instance.refreshProductsILikedByUserList()
    }
}
