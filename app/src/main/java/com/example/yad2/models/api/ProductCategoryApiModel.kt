package com.example.yad2.models.api

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ProductCategoryApiModel private constructor() {
    val BASE_URL = "https://fakestoreapi.com"
    var retrofit: Retrofit
    var productCategoryApi: ProductCategoryApi

    init {
        val gson = GsonBuilder()
            .setLenient()
            .create()
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        productCategoryApi = retrofit.create(ProductCategoryApi::class.java)
    }

    val productCategories: MutableLiveData<List<String>>
        get() {
            val data: MutableLiveData<List<String>> =
                MutableLiveData<List<String>>()
            val call: Call<List<String>?>? =
                productCategoryApi.getAllProductCategories()
            call?.enqueue(object : Callback<List<String>?> {
                override fun onResponse(
                    call: Call<List<String>?>,
                    response: Response<List<String>?>
                ) {
                    Log.d("TAG", call.toString());
                    Log.d("TAG", response.toString());
                    if (response.isSuccessful) {
                        val res: List<String>? = response.body()
                        val categories: List<String>? = res;

                        if (categories != null) {
                            data.value = categories!!
                        } else {
                            Log.d(
                                "TAG",
                                "----- getAllProductCategories response error - cant get categories"
                            )
                        }

                    } else {
                        Log.d("TAG", "----- getAllProductCategories response error")
                    }
                }

                override fun onFailure(call: Call<List<String>?>, t: Throwable) {
                    Log.d("TAG", "----- getAllProductCategories fail")
                }
            })
            return data
        }

    companion object {
        val _instance = ProductCategoryApiModel()
        fun instance(): ProductCategoryApiModel {
            return _instance
        }
    }
}
