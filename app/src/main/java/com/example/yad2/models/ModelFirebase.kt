package com.example.yad2.models

import Model
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import com.example.old2gold.model.Product.PRODUCTS_COLLECTION_NAME
import java.io.ByteArrayOutputStream
import java.util.Objects

class ModelFirebase {
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    var storage: FirebaseStorage = FirebaseStorage.getInstance()

    interface GetAllProductsListener {
        fun onComplete(list: List<Product>?)
    }

    interface GetLikedProductsListener {
        fun onComplete(list: List<Product>?)
    }

    interface GetMyProductsListener {
        fun onComplete(list: List<Product>?)
    }

    interface RemoveLikedProductsListener {
        fun onComplete()
    }

    interface AddLikedProductListener {
        fun onComplete()
    }

    fun saveProduct(product: Product, listener: AddProductListener) {
        val json: Map<String, Any> = product.toJson()
        db.collection(Product.PRODUCTS_COLLECTION_NAME)
            .document(product.getId())
            .set(json)
            .addOnSuccessListener { unused -> listener.onComplete() }
            .addOnFailureListener { e -> listener.onComplete() }
    }

    fun saveImage(
        imageBitmap: Bitmap,
        imageName: String,
        listener: SaveImageListener,
        directory: String
    ) {
        val storageRef: StorageReference = storage.getReference()
        val imgRef: StorageReference = storageRef.child("$directory/$imageName")
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val uploadTask: UploadTask = imgRef.putBytes(data)
        uploadTask.addOnFailureListener { exception -> listener.onComplete(null) }
            .addOnSuccessListener(object : OnSuccessListener<UploadTask.TaskSnapshot?>() {
                fun onSuccess(taskSnapshot: UploadTask.TaskSnapshot?) {
                    imgRef.getDownloadUrl()
                        .addOnSuccessListener { uri -> listener.onComplete(uri.toString()) }
                }
            })
    }

    fun addUser(user: User, id: String?) {
        val json: Map<String, Any> = user.toJson()
        db.collection(User.COLLECTION_NAME)
            .document(id)
            .set(json)
    }

    fun updateUser(updatedUser: User, listener: UpdateDataListener) {
        val json: Map<String, Any> = updatedUser.toJson()
        db.collection(User.COLLECTION_NAME)
            .document(updatedUser.getId())
            .set(json)
            .addOnCompleteListener { unused -> listener.onComplete() }
    }

    fun getUser(id: String?, optionalListener: GetLoggedUserListener): User? {
        val user: Array<User?> = arrayOfNulls<User>(1)
        if (id != null) {
            val docRef: DocumentReference = db.collection(User.COLLECTION_NAME)
                .document(id)
            docRef.get()
                .addOnCompleteListener(object : OnCompleteListener<DocumentSnapshot?>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    fun onComplete(task: Task<DocumentSnapshot?>) {
                        if (task.isSuccessful()) {
                            val document: DocumentSnapshot = task.getResult()
                            if (document.exists()) {
                                user[0] = document.toObject(User::class.java)
                                user[0].setId(document.getId())
                                optionalListener.onComplete(user[0])
                            } else {
                                Log.d("TAG", "No such document")
                            }
                        } else {
                            Log.d("TAG", "get failed with ", task.getException())
                        }
                    }
                })
        }
        return user[0]
    }

    fun getProductSellerUser(id: String?, optionalListener: GetLoggedUserListener): User? {
        val user: Array<User?> = arrayOfNulls<User>(1)
        val docRef: DocumentReference = db.collection(User.COLLECTION_NAME)
            .document(id)
        docRef.get()
            .addOnCompleteListener(object : OnCompleteListener<DocumentSnapshot?>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                fun onComplete(task: Task<DocumentSnapshot?>) {
                    if (task.isSuccessful()) {
                        val document: DocumentSnapshot = task.getResult()
                        if (document.exists()) {
                            user[0] = document.toObject(User::class.java)
                            user[0].setId(document.getId())
                        } else {
                            Log.d("TAG", "No such document")
                        }
                        optionalListener.onComplete(user[0])
                    } else {
                        Log.d("TAG", "get failed with ", task.getException())
                    }
                }
            })
        return user[0]
    }

    fun getUserO(id: String?, optionalListener: GetLoggedUserListener): User? {
        val user: Array<User?> = arrayOfNulls<User>(1)
        val docRef: DocumentReference = db.collection(User.COLLECTION_NAME)
            .document(id)
        docRef.get()
            .addOnCompleteListener(object : OnCompleteListener<DocumentSnapshot?>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                fun onComplete(task: Task<DocumentSnapshot?>) {
                    if (task.isSuccessful()) {
                        val document: DocumentSnapshot = task.getResult()
                        if (document.exists()) {
                            user[0] = document.toObject(User::class.java)
                            user[0].setId(document.getId())
                        } else {
                            Log.d("TAG", "No such document")
                        }
                        optionalListener.onComplete(user[0])
                    } else {
                        Log.d("TAG", "get failed with ", task.getException())
                    }
                }
            })
        return user[0]
    }

    fun getAllProducts(lastUpdateDate: Long?, listener: GetAllProductsListener): List<Product> {
        val products: MutableList<Product> = ArrayList<Product>()
        db.collection(PRODUCTS_COLLECTION_NAME)
            .whereGreaterThanOrEqualTo("updateDate", Timestamp(lastUpdateDate, 0))
            .orderBy("updateDate", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful()) {
                    createProductList(products, task)
                }
                listener.onComplete(products)
            }
        return products
    }

    private fun createProductList(products: MutableList<Product>, task: Task<QuerySnapshot>) {
        for (product in task.getResult()) {
            val productToAdd: Product = Product.create(Objects.requireNonNull(product.getData()))
            productToAdd.setId(product.getId())
            products.add(productToAdd)
        }
    }

    fun getProductById(productId: String?, listener: GetProductById) {
        db.collection(Product.PRODUCTS_COLLECTION_NAME)
            .document(productId)
            .get()
            .addOnCompleteListener(object : OnCompleteListener<DocumentSnapshot?>() {
                fun onComplete(task: Task<DocumentSnapshot?>) {
                    var product: Product? = null
                    if (task.isSuccessful() and (task.getResult() != null)) {
                        product = Product.create(task.getResult().getData())
                        product.setId(task.getResult().getId())
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
                if (task.isSuccessful()) {
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
            .document(id)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful()) {
                    val document: DocumentSnapshot = task.getResult()
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
                                            if (productsTask.isSuccessful()) {
                                                val result: DocumentSnapshot =
                                                    productsTask.getResult()
                                                val productToAdd: Product =
                                                    Product.create(Objects.requireNonNull(result.getData()))
                                                productToAdd.setId(result.getId())
                                                if (!productToAdd.isDeleted()) {
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
                    Log.d("TAG", "get failed with ", task.getException())
                }
            }
        return products
    }

    fun addToLikedProducts(productId: String?, addLikedProductListener: AddLikedProductListener) {
        val userId: String = Model.instance.mAuth.getUid()
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
        val userId: String = Model.instance.mAuth.getUid()
        db.collection(User.COLLECTION_NAME)
            .document(userId)
            .update("favoriteProducts", FieldValue.arrayRemove(productId))
            .addOnSuccessListener { unused -> removeLikedProductsListener.onComplete() }
    }
}
