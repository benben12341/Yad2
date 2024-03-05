package com.example.yad2.interfaces

import com.example.yad2.models.Product

interface GetProductByIdListener {
    fun onComplete(product: Product?)
}