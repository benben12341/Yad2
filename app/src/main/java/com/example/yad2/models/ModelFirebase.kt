package com.example.yad2.models

import android.graphics.Bitmap
import android.util.Log
import com.example.yad2.interfaces.AddLikedProductListener
import com.example.yad2.interfaces.GetAllProductsListener
import com.example.yad2.interfaces.GetLikedProductsListener
import com.example.yad2.interfaces.GetMyProductsListener
import com.example.yad2.interfaces.RemoveLikedProductsListener
import com.example.yad2.models.Product.Companion.PRODUCTS_COLLECTION_NAME
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import java.util.Objects

class ModelFirebase {
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var storage: FirebaseStorage
    private var mAuth: FirebaseAuth
    private var mUser: FirebaseUser?

    init {
        val settings: FirebaseFirestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        db.firestoreSettings = settings
        storage = FirebaseStorage.getInstance()
        mAuth = FirebaseAuth.getInstance()
        mUser = mAuth.currentUser
    }

    fun saveProduct(product: Product, listener: Model.AddProductListener) {
        val json: Map<String, Any?> = product.toJson()
        db.collection(PRODUCTS_COLLECTION_NAME)
            .document(product.id)
            .set(json)
            .addOnSuccessListener { listener.onComplete() }
            .addOnFailureListener { listener.onComplete() }
    }

    fun saveImage(
        imageBitmap: Bitmap,
        imageName: String,
        listener: Model.SaveImageListener,
        directory: String
    ) {
        val storageRef: StorageReference = storage.reference
        val imgRef: StorageReference = storageRef.child("$directory/$imageName")
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask: UploadTask = imgRef.putBytes(data)
        uploadTask.addOnFailureListener { listener.onComplete(null.toString()) }
            .addOnSuccessListener {
                imgRef.downloadUrl
                    .addOnSuccessListener { uri -> listener.onComplete(uri.toString()) }
            }
    }

    fun addUser(user: User, id: String?) {
        try {
            val json: Map<String, Any?> = user.toJson()
            db.collection(User.COLLECTION_NAME)
                .document(id!!)
                .set(json)
                .addOnSuccessListener {
                    Log.d("Firestore", "User added successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error adding user", e)
                }
        } catch (e: Exception) {
            Log.e("Firestore", "Exception while adding user", e)
        }
    }

    fun updateUser(updatedUser: User, listener: Model.UpdateDataListener) {
        val json: Map<String, Any?> = updatedUser.toJson()
        db.collection(User.COLLECTION_NAME)
            .document(updatedUser.id)
            .set(json)
            .addOnCompleteListener { listener.onComplete() }
    }

    fun getUser(id: String, optionalListener: Model.GetLoggedUserListener) {
        val documentRef = db.collection(User.COLLECTION_NAME).document(id)

        documentRef.get()
            .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        val user = document.toObject(User::class.java)
                        user?.id = document.id
                        optionalListener.onComplete(user ?: User("", "", "", "", "", "", ArrayList()))
                    } else {
                        optionalListener.onComplete(User("", "", "", "", "", "", ArrayList()))
                    }
                } else {
                    optionalListener.onComplete(User("", "", "", "", "", "", ArrayList()))
                }
            }
            .addOnFailureListener {
                optionalListener.onComplete(User("", "", "", "", "", "", ArrayList()))
            }
    }

    fun getAllProducts(lastUpdateDate: Long, listener: GetAllProductsListener): List<Product> {
        val products: MutableList<Product> = ArrayList()
        db.collection(PRODUCTS_COLLECTION_NAME)
            .whereGreaterThanOrEqualTo("updateDate", Timestamp(lastUpdateDate, 0))
            .orderBy("updateDate", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    createProductList(products, task)
                }
                listener.onComplete(products)
            }
        return products
    }

    private fun createProductList(products: MutableList<Product>, task: Task<QuerySnapshot>) {
        for (product in task.result) {
            val productToAdd: Product? = Product.create(Objects.requireNonNull(product.data))
            if (productToAdd != null) {
                productToAdd.id = product.id
            }
            if (productToAdd != null) {
                products.add(productToAdd)
            }
        }
    }

    fun getProductById(productId: String?, listener: Model.GetProductByIdListener) {
        db.collection(PRODUCTS_COLLECTION_NAME)
            .document(productId!!)
            .get()
            .addOnCompleteListener { task ->
                var product: Product? = null
                if (task.isSuccessful and (task.result != null)) {
                    product = task.result?.data?.let { Product.create(it) }
                    if (product != null) {
                        product.id = task.result!!.id
                    }
                }
                listener.onComplete(product!!)
            }
    }

    fun getProductsByUser(id: String?, myProductsListener: GetMyProductsListener): List<Product> {
        val products: MutableList<Product> = ArrayList()
        FirebaseFirestore.getInstance().collection(PRODUCTS_COLLECTION_NAME)
            .whereEqualTo("contactId", id)
            .whereEqualTo("isDeleted", false)
            .orderBy("updateDate", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    createProductList(products, task)
                }
                myProductsListener.onComplete(products)
            }
        return products
    }

    fun getAllLikedProductsByUser(
        id: String?,
        myProductsListener: GetLikedProductsListener
    ): List<Product> {
        val user: Array<User?> = arrayOfNulls(1)
        val products: MutableList<Product> = ArrayList()
        db.collection(User.COLLECTION_NAME)
            .document(id!!)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document: DocumentSnapshot = task.result
                    if (document.exists()) {
                        user[0] = document.toObject(User::class.java)
                        val favoriteProducts: ArrayList<String>? = user[0]?.favoriteProducts
                        if (favoriteProducts != null) {
                            if (favoriteProducts.stream().count() != 0L) {
                                for (i in favoriteProducts.indices) {
                                    FirebaseFirestore.getInstance()
                                        .collection(PRODUCTS_COLLECTION_NAME)
                                        .document(favoriteProducts[i])
                                        .get()
                                        .addOnCompleteListener { productsTask ->
                                            if (productsTask.isSuccessful) {
                                                val result: DocumentSnapshot =
                                                    productsTask.result
                                                val productToAdd: Product? =
                                                    Product.create(Objects.requireNonNull(result.data))
                                                if (productToAdd != null) {
                                                    productToAdd.id = result.id
                                                }
                                                if (productToAdd != null) {
                                                    if (!productToAdd.isDeleted) {
                                                        products.add(productToAdd)
                                                        myProductsListener.onComplete(products)
                                                    }
                                                }
                                            }
                                        }
                                }
                            } else {
                                myProductsListener.onComplete(products)
                            }
                        }
                    } else {
                        Log.d("TAG", "No such document")
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.exception)
                }
            }
        return products
    }

    fun addToLikedProducts(productId: String?, addLikedProductListener: AddLikedProductListener) {
        val userId: String = Model.instance.mAuth.uid.toString()
        db.collection(User.COLLECTION_NAME)
            .document(userId)
            .update("favoriteProducts", FieldValue.arrayUnion(productId))
            .addOnSuccessListener { addLikedProductListener.onComplete() }
    }

    fun removeFromFavoriteList(
        productId: String?,
        removeLikedProductsListener: RemoveLikedProductsListener
    ) {
        val userId: String = Model.instance.mAuth.uid.toString()
        db.collection(User.COLLECTION_NAME)
            .document(userId)
            .update("favoriteProducts", FieldValue.arrayRemove(productId))
            .addOnSuccessListener { removeLikedProductsListener.onComplete() }
    }
}
