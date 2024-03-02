package com.example.yad2.models.cb

import com.example.yad2.enums.Size

class ProductSizeCB private constructor(var productSize: String) {
    fun getProductSize(): String? {
        return this.productSize
    }
    fun setProductTypes(productType: String) {
        productSize = productType
    }

    var isFlag = false

    companion object {
        val allCheckboxSizes: List<ProductSizeCB>
            get() {
                val productFilterCBList: MutableList<ProductSizeCB> = mutableListOf()
                val productSizes: Array<Size> = Size.entries.toTypedArray()
                for (i in productSizes.indices) {
                    productFilterCBList.add(i, ProductSizeCB(productSizes[i].getProductSize()!!))
                }
                return productFilterCBList
            }
    }
}
