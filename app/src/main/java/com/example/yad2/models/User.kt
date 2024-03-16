package com.example.yad2.models

import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.NonNull
import lombok.Setter
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
class User() : Serializable {

    @PrimaryKey
    var id: String = ""
    var firstName: String? = null
    var lastName: String? = null
    var email: String? = null
    var phoneNumber: String? = null
    var address: String? = null
    var favoriteProducts: ArrayList<String>? = null
    var userImageUrl: String? = null

    constructor(
        id: String,
        firstName: String?,
        lastName: String?,
        email: String?,
        phoneNumber: String?,
        address: String?,
        favoriteProducts: ArrayList<String>?
    ) : this() {
        this.id = id
        this.firstName = firstName
        this.lastName = lastName
        this.email = email
        this.phoneNumber = phoneNumber
        this.address = address
        this.favoriteProducts = favoriteProducts
    }

    fun toJson(): Map<String, Any?> {
        val json: MutableMap<String, Any?> = HashMap()
        json["firstName"] = firstName
        json["lastName"] = lastName
        json["email"] = email
        json["phoneNumber"] = phoneNumber
        json["address"] = address
        json["userImageUrl"] = userImageUrl
        json["favoriteProducts"] = emptyList<Any>()
        return json
    }

    companion object {
        const val COLLECTION_NAME = "users"

        fun create(json: Map<String?, Any?>): User {
            val currentUser = JSONObject(json)
            val id = json["id"] as String
            val firstName = json["firstName"] as String?
            val lastName = json["lastName"] as String?
            val address = json["address"] as String?
            val email = json["email"] as String?
            val phoneNumber = json["phoneNumber"] as String?
            val userImageUrl = json["userImageUrl"] as String?
            val favoriteProducts =
                ArrayList<String>()
            try {
                val userFavoriteProducts =
                    currentUser.getJSONArray("favoriteProducts")
                for (index in 0 until userFavoriteProducts.length()) {
                    favoriteProducts.add(userFavoriteProducts[index] as String)
                }
            } catch (e: JSONException) {
                Log.d("error", "failed getting user favorite product")
            }
            return User(
                id,
                firstName,
                lastName,
                email,
                phoneNumber,
                address,
                favoriteProducts
            )
        }
    }
}
