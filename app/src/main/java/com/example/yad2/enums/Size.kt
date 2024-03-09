package com.example.yad2.enums

import android.annotation.SuppressLint

enum class Size(val productSize: String) {
    Y("Y"),
    XS("XS"),
    S("S"),
    M("M"),
    L("L"),
    XL("XL"),
    XXL("XXL");


    private var sizeString: String = toString()

    @SuppressLint("NotConstructor")
    open fun Size(genderString: String) {
        sizeString = genderString
    }

    override fun toString(): String {
        return sizeString
    }
}
