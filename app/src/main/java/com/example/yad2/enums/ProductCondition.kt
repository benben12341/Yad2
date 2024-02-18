package com.example.yad2.enums

enum class ProductCondition(private val productCondition: String) {
    GOOD_AS_NEW("Good as new"),
    GOOD("Good"),
    OK("Ok");

    override fun toString(): String {
        return productCondition
    }
}
