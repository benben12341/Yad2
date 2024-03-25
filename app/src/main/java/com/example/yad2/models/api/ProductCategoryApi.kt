package com.example.yad2.models.api

import retrofit2.Call
import retrofit2.http.GET


interface ProductCategoryApi {
    @GET("/products/categories")
    fun getAllProductCategories(): Call<List<String>?>?
}
