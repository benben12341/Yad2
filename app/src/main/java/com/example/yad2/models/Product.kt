import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter

@Getter
@Setter
@NoArgsConstructor
@Entity
class Product(
    @field:PrimaryKey var id: String,
    var title: String?,
    var description: String?,
    var gender: String?,
    var productCondition: String?,
    var productCategory: String?,
    var price: String?,
    var contactId: String?,
    var latitude: Double?,
    var longitude: Double?,
    isDeleted: Boolean,
    isSold: Boolean
) {
    var imageUrl: String? = null
    var updateDate: Long = 0
    var isDeleted = false
    var isSold = false

    init {
        this.isDeleted = isDeleted
        this.isSold = isSold
    }

    fun toJson(): Map<String, Any?> {
        val json: MutableMap<String, Any?> = HashMap()
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

    companion object {
        const val PRODUCTS_COLLECTION_NAME = "products"
        fun create(json: Map<String?, Any?>): Product {
            val id = json["id"] as String?
            val title = json["title"] as String?
            val description = json["description"] as String?
            val price = json["price"] as String?
            val gender = json["gender"] as String?
            val condition = json["condition"] as String?
            val productCategory = json["productCategory"] as String?
            val imageUrl = json["imageUrl"] as String?
            val contactId = json["contactId"] as String?
            val ts: Timestamp? = json["updateDate"] as Timestamp?
            val updateDate: Long = ts.getSeconds()
            val latitude = json["latitude"] as Double?
            val longitude = json["longitude"] as Double?
            val isDeleted = json.containsKey("isDeleted") && json["isDeleted"] as Boolean
            val isSold = json.containsKey("isSold") && json["isSold"] as Boolean
            val product = Product(
                id!!, title, description, gender, condition,
                productCategory, price, contactId, latitude, longitude, isDeleted, isSold
            )
            product.setImageUrl(imageUrl)
            product.setUpdateDate(updateDate)
            return product
        }
    }
}
