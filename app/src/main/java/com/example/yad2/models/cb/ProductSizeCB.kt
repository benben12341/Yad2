package com.example.yad2.models.cb

import com.example.old2gold.enums.Size

class ProductSizeCB private constructor(var productSize: String) {
    fun setProductTypes(productType: String) {
        productSize = productType
    }

    var isFlag = false

    companion object {
        val allCheckboxSizes: List<ProductSizeCB>
            get() {
                val productFilterCBList: List<ProductSizeCB> = ArrayList()
                val productSizes: Array<Size> = Size.values()
                for (i in productSizes.indices) {
                    productFilterCBList.add(i, ProductSizeCB(productSizes[i].getProductSize()))
                }
                return productFilterCBList
            }
    }
}
