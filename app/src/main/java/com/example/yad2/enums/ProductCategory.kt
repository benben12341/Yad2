package com.example.yad2.enums

enum class ProductCategory(private val productCategory: String) {
    PANTS("Pants"),
    SHIRTS("Shirts"),
    SKIRTS("Skirts"),
    DRESSES("Dresses"),
    JUMPERS("Jumpers"),
    ACCESSORIES("Accessories"),
    OTHER("Other");

    override fun toString(): String {
        return productCategory
    }
}
