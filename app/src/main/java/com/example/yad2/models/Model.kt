package com.example.yad2.models

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.yad2.AppLocalDb
import com.example.yad2.interfaces.GetProductById
import com.google.firebase.auth.FirebaseAuth
import java.security.AccessController.getContext
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class Model private constructor() {
    val executor: Executor = Executors.newFixedThreadPool(1)
    val modelFirebase: ModelFirebase = ModelFirebase()
    val productsList: MutableLiveData<List<Product>> = MutableLiveData()
    val favoriteProductsByUserList: MutableLiveData<List<Product>> = MutableLiveData()
    val productsByUserList: MutableLiveData<List<Product>> = MutableLiveData()
    val productListLoadingState: MutableLiveData<ProductsListLoadingState> = MutableLiveData()
    val userProductsLoadingState: MutableLiveData<ProductsListLoadingState> = MutableLiveData()
    val favoriteProductsLoadingState: MutableLiveData<ProductsListLoadingState> = MutableLiveData()
    val categoriesFilterList: MutableLiveData<List<String>> = MutableLiveData()
    val loggedUser: MutableLiveData<User> = MutableLiveData()

    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    enum class ProductsListLoadingState {
        LOADING,
        LOADED
    }

    interface AddProductListener {
        fun onComplete()
    }

    interface GetLoggedUserListener {
        fun onComplete(user: User)
    }

    fun interface UpdateDataListener {
        fun onComplete()
    }

    fun interface SaveImageListener {
        fun onComplete(url: String)
    }

    val userProductsLoadingState: LiveData<ProductsListLoadingState>
        get() = userProductsLoadingState

    val favoritesProductsLoadingState: LiveData<ProductsListLoadingState>
        get() = favoriteProductsLoadingState

    init {
        productListLoadingState.value = ProductsListLoadingState.LOADED
        userProductsLoadingState.value = ProductsListLoadingState.LOADED
        favoriteProductsLoadingState.value = ProductsListLoadingState.LOADED
    }

    fun getAll(): LiveData<List<Product>> {
        if (productsList.value == null) {
            categoriesFilterList.postValue(ArrayList())
            refreshProductsList()
        }
        return productsList
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun getLoggedUser(): LiveData<User> {
        if (loggedUser.value == null) {
            refreshLoggedUser()
        }
        return loggedUser
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun getAllFavoriteProductsByUser(): LiveData<List<Product>> {
        if (favoriteProductsByUserList.value == null) {
            refreshProductsILikedByUserList()
        }
        return favoriteProductsByUserList
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun getProductOfUser(): LiveData<List<Product>> {
        if (productsByUserList.value == null) {
            refreshProductsByMyUser()
        }
        return productsByUserList
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun getProductListByTypeFilter(selectedCategories: List<String>) {
        categoriesFilterList.postValue(selectedCategories)
        refreshProductsList()
    }

    fun isLoggedUser(product: Product): Boolean {
        val loggedUserId: String? = mAuth.uid
        return product.contactId != null && product.contactId == loggedUserId
    }

    fun isInFilters(product: Product): Boolean {
        val categories: List<String> = categoriesFilterList.value ?: ArrayList()
        return categories.contains(product.productCategory) || categories.isEmpty()
    }

    fun refreshProductsList() {
        productListLoadingState.postValue(ProductsListLoadingState.LOADING)
        val lastUpdateDate: Long = MyApplication.getContext().getSharedPreferences("TAG", Context.MODE_PRIVATE).getLong("lastUpdate", 0)
        modelFirebase.getAllProducts(lastUpdateDate) { allProducts ->
            executor.execute {
                var lud: Long = 0
                Log.d("TAG", "fb returned " + allProducts.size)
                lud = getProductsLastUpdateDate(lud, allProducts)
                updateLastLocalUpdateDate(lud)
                val productList: List<Product> = AppLocalDb.db.productDao().getAll()
                    .filter { product -> !isLoggedUser(product) && isInFilters(product) }
                productsList.postValue(productList)
                productListLoadingState.postValue(ProductsListLoadingState.LOADED)
            }
        }
    }

    private fun getProductsLastUpdateDate(lud: Long, allProducts: List<Product>): Long {
        var lastUpdateDate = lud
        for (product in allProducts) {
            AppLocalDb.db.productDao().insertAll(product)
            if (lastUpdateDate < product.updateDate) {
                lastUpdateDate = product.updateDate
            }
        }
        return lastUpdateDate
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun refreshProductsILikedByUserList() {
        favoriteProductsLoadingState.postValue(ProductsListLoadingState.LOADING)
        modelFirebase.getAllLikedProductsByUser(mAuth.uid) { products ->
            executor.execute {
                var lud: Long = 0
                Log.d("TAG", "fb returned " + products.size)
                updateLastLocalUpdateDate(lud)
                favoriteProductsByUserList.postValue(products)
                favoriteProductsLoadingState.postValue(ProductsListLoadingState.LOADED)
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun refreshProductsByMyUser() {
        userProductsLoadingState.postValue(ProductsListLoadingState.LOADING)
        val id: String = mAuth.uid
        modelFirebase.getProductsByUser(id) { products ->
            executor.execute {
                var lastUpdateDate: Long = 0
                Log.d("TAG", "fb returned " + products.size)
                updateLastLocalUpdateDate(getProductsLastUpdateDate(lastUpdateDate, products))
                val productList: List<Product> = AppLocalDb.db.productDao().getProductsByContactId(id)
                productsByUserList.postValue(productList)
                userProductsLoadingState.postValue(ProductsListLoadingState.LOADED)
            }
        }
    }

    private fun updateLastLocalUpdateDate(lastUpdateDate: Long) {
        getContext()
            .getSharedPreferences("TAG", Context.MODE_PRIVATE)
            .edit()
            .putLong("updateDate", lastUpdateDate)
            .apply()
    }

    fun saveProduct(product: Product, listener: AddProductListener) {
        modelFirebase.saveProduct(product) {
            listener.onComplete()
            refreshProductsList()
        }
    }

    fun saveProductImage(imageBitmap: Bitmap, imageName: String, listener: SaveImageListener) {
        modelFirebase.saveImage(imageBitmap, imageName, listener, "products")
    }

    fun saveUserImage(imageBitmap: Bitmap, imageName: String, listener: SaveImageListener) {
        modelFirebase.saveImage(imageBitmap, imageName, listener, "users")
    }

    fun saveUser(user: User, id: String) {
        modelFirebase.addUser(user, id)
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun updateUser(user: User, listener: UpdateDataListener) {
        modelFirebase.updateUser(user, listener)
        refreshLoggedUser()
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun getUser(id: String, optionalListener: (User) -> MenuItem): User {
        return modelFirebase.getUser(id, optionalListener)
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun refreshLoggedUser(): User {
        return modelFirebase.getUserO(mAuth.uid) { user ->
            loggedUser.postValue(user)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun getProductSellerUser(id: String, optionalListener: GetLoggedUserListener): User {
        return modelFirebase.getProductSellerUser(id, optionalListener)
    }

    fun getProductById(productId: String, listener: GetProductById) {
        modelFirebase.getProductById(productId, listener)
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun removeFromLikedProducts(productId: String, listener: ModelFirebase.RemoveLikedProductsListener) {
        modelFirebase.removeFromFavoriteList(productId) {
            listener.onComplete()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun addToLikedProducts(productId: String, likedProductListener: ModelFirebase.AddLikedProductListener) {
        modelFirebase.addToLikedProducts(productId) {
            likedProductListener.onComplete()
        }
    }

    companion object {
        @JvmField
        val instance = Model()
    }
}
