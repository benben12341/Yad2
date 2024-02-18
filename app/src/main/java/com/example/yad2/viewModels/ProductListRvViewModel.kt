package com.example.yad2.viewModels

import Model
import Product
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.logging.Filter

class ProductListRvViewModel : ViewModel() {
    var filteredProducts: LiveData<List<Product>>
    var originalProducts: LiveData<List<Product>>
    var categoriesFilterList: LiveData<List<Product>>? = null
    private val filters = MutableLiveData<Set<Filter>>()
    @RequiresApi(api = Build.VERSION_CODES.N)
    fun addTypeFilter(categories: List<String?>?) {
        Model.instance.getProductListByTypeFilter(categories)
    }

    init {
        val all: LiveData<List<Product>> = Model.instance.getAll()
        filteredProducts = all
        originalProducts = all
    }

    fun refreshProducts() {
        Model.instance.refreshProductsList()
    }

    val data: LiveData<List<Any>>
        get() = filteredProducts
}
