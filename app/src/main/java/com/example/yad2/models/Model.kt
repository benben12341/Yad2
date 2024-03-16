package com.example.yad2.models

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.yad2.AppLocalDb
import com.example.yad2.MyApplication
import com.example.yad2.interfaces.AddLikedProductListener
import com.example.yad2.interfaces.GetAllProductsListener
import com.example.yad2.interfaces.GetLikedProductsListener
import com.example.yad2.interfaces.GetMyProductsListener
import com.example.yad2.interfaces.GetProductByIdListener
import com.example.yad2.interfaces.RemoveLikedProductsListener
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class Model() {
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

    fun interface AddProductListener {
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

    val favoritesProductsLoadingState: LiveData<ProductsListLoadingState>
        get() = favoriteProductsLoadingState

    init {
        productListLoadingState.value = ProductsListLoadingState.LOADED
        userProductsLoadingState.value = ProductsListLoadingState.LOADED
        favoriteProductsLoadingState.value = ProductsListLoadingState.LOADED
    }

    fun getAll(): LiveData<List<Product>> {
        Log.i("Main",productsList.value.toString())
        if (productsList.value == null) {
            categoriesFilterList.postValue(ArrayList())
            refreshProductsList()
        }
        return productsList
    }

    fun getLoggedUser(): LiveData<User> {
        if (loggedUser.value == null) {
            refreshLoggedUser()
        }
        return loggedUser
    }

    fun getAllFavoriteProductsByUser(): LiveData<List<Product>> {
        if (favoriteProductsByUserList.value == null) {
            refreshProductsILikedByUserList()
        }
        return favoriteProductsByUserList
    }

    fun getProductOfUser(): LiveData<List<Product>> {
        if (productsByUserList.value == null) {
            refreshProductsByMyUser()
        }
        return productsByUserList
    }

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
        val lastUpdateDate: Long =
            MyApplication.context!!.getSharedPreferences("TAG", Context.MODE_PRIVATE)
                .getLong("lastUpdate", 0);
        modelFirebase.getAllProducts(lastUpdateDate, object : GetAllProductsListener {
            override fun onComplete(list: List<Product>?) {
                // Your logic when all products are retrieved
                executor.execute {
                    var lud: Long = 0
                    Log.d("TAG", "fb returned " + list!!.size)
                    lud = getProductsLastUpdateDate(lud, list)
                    updateLastLocalUpdateDate(lud)
                    val productList: List<Product> =
                        (AppLocalDb.db.productDao()!!.all()!! as List<Product>)
                            .filter { product: Product ->
                                !isLoggedUser(product) && isInFilters(
                                    product
                                )
                            }
                    productsList.postValue(productList)
                    productListLoadingState.postValue(ProductsListLoadingState.LOADED)
                }
            }
        });
    }

    private fun getProductsLastUpdateDate(lud: Long, allProducts: List<Product>): Long {
        var lastUpdateDate = lud
        for (product in allProducts) {
            AppLocalDb.db.productDao()!!.insertAll(product)
            if (lastUpdateDate < product.updateDate) {
                lastUpdateDate = product.updateDate
            }
        }
        return lastUpdateDate
    }

    fun refreshProductsILikedByUserList() {
        favoriteProductsLoadingState.postValue(ProductsListLoadingState.LOADING);

        modelFirebase.getAllLikedProductsByUser(mAuth.uid, object : GetLikedProductsListener {
            override fun onComplete(list: List<Product>?) {
                executor.execute {
                    val lud: Long = 0
                    Log.d("TAG", "fb returned " + list!!.size)
                    updateLastLocalUpdateDate(lud)
                    favoriteProductsByUserList.postValue(list!!)
                    favoriteProductsLoadingState.postValue(ProductsListLoadingState.LOADED)
                }
            }
        });
    }

    fun refreshProductsByMyUser() {
        userProductsLoadingState.postValue(ProductsListLoadingState.LOADING)
        val id: String? = mAuth.uid
        modelFirebase.getProductsByUser(id, object : GetMyProductsListener {
            override fun onComplete(list: List<Product>?) {
                executor.execute {
                    val lastUpdateDate: Long = 0
                    Log.d("TAG", "fb returned " + list!!.size)
                    updateLastLocalUpdateDate(getProductsLastUpdateDate(lastUpdateDate, list))
                    val productList: List<Product>? =
                        AppLocalDb.db.productDao()!!.getProductsByContactId(id)
                    productsByUserList.postValue(productList!!)
                    userProductsLoadingState.postValue(ProductsListLoadingState.LOADED)
                }
            }
        });
    }

    private fun updateLastLocalUpdateDate(lastUpdateDate: Long) {
        MyApplication.context!!.getSharedPreferences("TAG", Context.MODE_PRIVATE)
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

    fun updateUser(user: User, listener: UpdateDataListener) {
        modelFirebase.updateUser(user, listener)
        refreshLoggedUser()
    }

    fun getUser(id: String, optionalListener: GetLoggedUserListener) {
        return modelFirebase.getUser(id, optionalListener)
    }

    fun refreshLoggedUser(): User? {
        return modelFirebase.getUserO(mAuth.uid, object : GetLoggedUserListener {
            override fun onComplete(user: User) {
                loggedUser.postValue(user)
            }
        })
    }

    fun getProductSellerUser(id: String, optionalListener: GetLoggedUserListener): User? {
        return modelFirebase.getProductSellerUser(id, optionalListener)
    }

    fun getProductById(productId: String, listener: GetProductByIdListener) {
        modelFirebase.getProductById(productId, listener)
    }

    fun removeFromLikedProducts(
        productId: String,
        listener: RemoveLikedProductsListener
    ) {
        modelFirebase.removeFromFavoriteList(productId, listener);
    }

    fun addToLikedProducts(
        productId: String,
        likedProductListener: AddLikedProductListener
    ) {
        modelFirebase.addToLikedProducts(productId, likedProductListener)
    }

    companion object {
        @JvmField
        val instance = Model()
    }
}
