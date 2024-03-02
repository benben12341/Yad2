package com.example.yad2.models

import com.example.yad2.models.User
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.yad2.interfaces.AddLikedProductListener
import com.example.yad2.interfaces.GetAllProductsListener
import com.example.yad2.interfaces.GetLikedProductsListener
import com.example.yad2.interfaces.GetMyProductsListener
import com.example.yad2.interfaces.GetProductById
import com.example.yad2.interfaces.RemoveLikedProductsListener
import com.example.yad2.models.Product.Companion.PRODUCTS_COLLECTION_NAME
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import java.util.Objects

class ModelFirebase {
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    var storage: FirebaseStorage = FirebaseStorage.getInstance()

    fun saveProduct(product: Product, listener: Model.AddProductListener) {
        val json: Map<String, Any?> = product.toJson()
        db.collection(PRODUCTS_COLLECTION_NAME)
            .document(product.id)
            .set(json)
            .addOnSuccessListener { unused -> listener.onComplete() }
            .addOnFailureListener { e -> listener.onComplete() }
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
        uploadTask.addOnFailureListener { exception -> listener.onComplete(null.toString()) }
            .addOnSuccessListener(object : OnSuccessListener<UploadTask.TaskSnapshot?> {
                override fun onSuccess(taskSnapshot: UploadTask.TaskSnapshot?) {
                    imgRef.downloadUrl
                        .addOnSuccessListener { uri -> listener.onComplete(uri.toString()) }
                }
            })
    }

    fun addUser(user: User, id: String?) {
        val json: Map<String, Any?> = user.toJson()
        db.collection(User.COLLECTION_NAME)
            .document(id!!)
            .set(json)
    }

    fun updateUser(updatedUser: User, listener: Model.UpdateDataListener) {
        val json: Map<String, Any?> = updatedUser.toJson()
        db.collection(User.COLLECTION_NAME)
            .document(updatedUser.id.toString())
            .set(json)
            .addOnCompleteListener { unused -> listener.onComplete() }
    }

    fun getUser(id: String?, optionalListener: Model.GetLoggedUserListener): User? {
        val user: Array<User?> = arrayOfNulls<User>(1)
        if (id != null) {
            val docRef: DocumentReference = db.collection(User.COLLECTION_NAME)
                .document(id)
            docRef.get()
                .addOnCompleteListener(object : OnCompleteListener<DocumentSnapshot?> {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    override fun onComplete(task: Task<DocumentSnapshot?>) {
                        if (task.isSuccessful) {
                            val document: DocumentSnapshot = task.result!!
                            if (document.exists()) {
                                user[0] = document.toObject(User::class.java)
                                user[0]?.id = document.id.toString()
                                optionalListener.onComplete(user[0]!!)
                            } else {
                                Log.d("TAG", "No such document")
                            }
                        } else {
                            Log.d("TAG", "get failed with ", task.exception)
                        }
                    }
                })
        }
        return user[0]
    }

    fun getProductSellerUser(id: String?, optionalListener: Model.GetLoggedUserListener): User? {
        val user: Array<User?> = arrayOfNulls<User>(1)
        val docRef: DocumentReference = db.collection(User.COLLECTION_NAME)
            .document(id!!)
        docRef.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document: DocumentSnapshot = task.result!!
                    if (document.exists()) {
                        user[0] = document.toObject(User::class.java)
                        user[0]?.id = document.id
                    } else {
                        Log.d("TAG", "No such document")
                    }
                    optionalListener.onComplete(user[0]!!)
                } else {
                    Log.d("TAG", "get failed with ", task.exception)
                }
            }
        return user[0]
    }

    fun getUserO(id: String?, optionalListener: Model.GetLoggedUserListener): User? {
        val user: Array<User?> = arrayOfNulls<User>(1)
        val docRef: DocumentReference = db.collection(User.COLLECTION_NAME)
            .document(id!!)
        docRef.get()
            .addOnCompleteListener(object : OnCompleteListener<DocumentSnapshot?> {
                @RequiresApi(api = Build.VERSION_CODES.N)
                override fun onComplete(task: Task<DocumentSnapshot?>) {
                    if (task.isSuccessful) {
                        val document: DocumentSnapshot = task.result!!
                        if (document.exists()) {
                            user[0] = document.toObject(User::class.java)
                            user[0]?.id = document.id
                        } else {
                            Log.d("TAG", "No such document")
                        }
                        optionalListener.onComplete(user[0]!!)
                    } else {
                        Log.d("TAG", "get failed with ", task.exception)
                    }
                }
            })
        return user[0]
    }

    fun getAllProducts(lastUpdateDate: Long, listener: GetAllProductsListener): List<Product> {
        val products: MutableList<Product> = ArrayList<Product>()
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
            val productToAdd: Product = Product.create(Objects.requireNonNull(product.data))
            productToAdd.id = product.id
            products.add(productToAdd)
        }
    }

    fun getProductById(productId: String?, listener: GetProductById) {
        db.collection(PRODUCTS_COLLECTION_NAME)
            .document(productId!!)
            .get()
            .addOnCompleteListener(object : OnCompleteListener<DocumentSnapshot?>() {
                override fun onComplete(task: Task<DocumentSnapshot?>) {
                    var product: Product? = null
                    if (task.isSuccessful and (task.result != null)) {
                        product = Product.create(task.result?.getData())
                        if (product != null) {
                            product.id = task.result!!.id
                        }
                    }
                    listener.onComplete(product)
                }
            })
    }

    fun getProductsByUser(id: String?, myProductsListener: GetMyProductsListener): List<Product> {
        val products: MutableList<Product> = ArrayList<Product>()
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun getAllLikedProductsByUser(
        id: String?,
        myProductsListener: GetLikedProductsListener
    ): List<Product> {
        val user: Array<User?> = arrayOfNulls<User>(1)
        val products: MutableList<Product> = ArrayList<Product>()
        db.collection(User.COLLECTION_NAME)
            .document(id!!)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document: DocumentSnapshot = task.result
                    if (document.exists()) {
                        user[0] = document.toObject(User::class.java)
                        val favoriteProducts: ArrayList<String> = user[0].getFavoriteProducts()
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
                                                val productToAdd: Product =
                                                    Product.create(Objects.requireNonNull(result.data))
                                                productToAdd.id = result.id
                                                if (!productToAdd.isDeleted) {
                                                    products.add(productToAdd)
                                                    myProductsListener.onComplete(products)
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
            .addOnSuccessListener { unused -> addLikedProductListener.onComplete() }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun removeFromFavoriteList(
        productId: String?,
        removeLikedProductsListener: RemoveLikedProductsListener
    ) {
        val userId: String = Model.instance.mAuth.uid.toString()
        db.collection(User.COLLECTION_NAME)
            .document(userId)
            .update("favoriteProducts", FieldValue.arrayRemove(productId))
            .addOnSuccessListener { unused -> removeLikedProductsListener.onComplete() }
    }
}
