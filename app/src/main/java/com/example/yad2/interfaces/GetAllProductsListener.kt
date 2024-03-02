package com.example.yad2.interfaces

import com.example.yad2.models.Product

interface GetAllProductsListener {
        fun onComplete(list: List<Product>?)
    }