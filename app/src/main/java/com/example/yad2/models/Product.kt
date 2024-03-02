package com.example.yad2.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

@Entity
data class Product(
    @PrimaryKey
    var id: String,
    var title: String?,
    var description: String?,
    var gender: String?,
    var productCondition: String?,
    var productCategory: String?,
    var imageUrl: String?,
    var contactId: String?,
    var price: String?,
    var updateDate: Long = 0,
    var longitude: Double?,
    var latitude: Double?,
    var isDeleted: Boolean = false,
    var isSold: Boolean = false
) {
    companion object {
        const val PRODUCTS_COLLECTION_NAME = "products"
    }

    constructor(
        id: String,
        title: String?,
        description: String?,
        gender: String?,
        productCondition: String?,
        productCategory: String?,
        price: String?,
        contactId: String?,
        latitude: Double?,
        longitude: Double?,
        isDeleted: Boolean,
        isSold: Boolean
    ) : this(
        id, title, description, gender, productCondition, productCategory, null,
        contactId, price, 0, longitude, latitude, isDeleted, isSold
    )

    fun toJson(): Map<String, Any?> {
        val json = HashMap<String, Any?>()
        json["id"] = id
        json["title"] = title
        json["description"] = description
        json["gender"] = gender
        json["condition"] = productCondition
        json["productCategory"] = productCategory
        json["imageUrl"] = imageUrl
        json["contactId"] = contactId
        json["longitude"] = longitude
        json["latitude"] = latitude
        json["updateDate"] = FieldValue.serverTimestamp()
        json["price"] = price
        json["isDeleted"] = isDeleted
        json["isSold"] = isSold
        return json
    }

     fun create(json: Map<String?, Any?>): Product? {
        val id = json["id"] as String?
        val title = json["title"] as String?
        val description = json["description"] as String?
        val price = json["price"] as String?
        val gender = json["gender"] as String?
        val condition = json["condition"] as String?
        val productCategory = json["productCategory"] as String?
        val imageUrl = json["imageUrl"] as String?
        val contactId = json["contactId"] as String?
        val ts = json["updateDate"] as Timestamp?
        val updateDate = ts!!.seconds
        val latitude = json["latitude"] as Double?
        val longitude = json["longitude"] as Double?
        val isDeleted = json.containsKey("isDeleted") && json["isDeleted"] as Boolean
        val isSold = json.containsKey("isSold") && json["isSold"] as Boolean
        val product: Product = Product(
            id.toString(), title, description, gender, condition,
            productCategory, price, contactId, latitude, longitude, isDeleted, isSold
        )
        product.imageUrl = imageUrl
        product.updateDate = updateDate
        return product
    }
}
