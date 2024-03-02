package com.example.yad2.interfaces

import com.example.yad2.models.Product

interface GetLikedProductsListener {
        fun onComplete(list: List<Product>?)
    }