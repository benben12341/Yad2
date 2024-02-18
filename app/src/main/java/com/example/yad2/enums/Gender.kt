package com.example.yad2.enums

enum class Gender(private val genderString: String) {
    FEMALE("Female"),
    MALE("Male"),
    OTHER("Other");

    override fun toString(): String {
        return genderString
    }
}