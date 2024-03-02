package com.example.yad2.models.cb

import com.example.yad2.enums.ProductCategory

class ProductFilterCB private constructor(var productType: String) {
    fun setProductTypes(productType: String) {
        this.productType = productType
    }

    var isFlag = false

    companion object {
        val allCheckboxTypes: List<ProductFilterCB>
            get() {
                val productSizeFilterCBList: MutableList<ProductFilterCB> = mutableListOf()
                val productTypes: Array<ProductCategory> = ProductCategory.values()
                for (i in productTypes.indices) {
                    productSizeFilterCBList.add(i, ProductFilterCB(productTypes[i].toString()))
                }
                return productSizeFilterCBList
            }
    }
}