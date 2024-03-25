package com.example.yad2.models.cb

class ProductFilterCB public constructor(var productType: String) {
    fun setProductTypes(productType: String) {
        this.productType = productType
    }

    var isFlag = false
}