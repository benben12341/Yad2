package com.example.yad2.interfaces

import com.example.yad2.models.Product

interface GetProductById {
    fun onComplete(product: Product?)
}