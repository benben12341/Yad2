package com.example.yad2.interfaces

import com.example.yad2.models.Product

interface GetMyProductsListener {
    fun onComplete(list: List<Product>?)
}