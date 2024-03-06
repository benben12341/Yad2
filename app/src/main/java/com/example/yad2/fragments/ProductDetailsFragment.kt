package com.example.yad2.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import com.example.yad2.R
import com.example.yad2.interfaces.AddLikedProductListener
import com.example.yad2.interfaces.RemoveLikedProductsListener
import com.example.yad2.models.Model
import com.example.yad2.models.Product
import com.example.yad2.models.User
import com.example.yad2.viewModels.FavoriteProductListRvViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import java.text.MessageFormat

open class ProductDetailsFragment : Fragment() {
    private var imageUrl: ImageView? = null
    var sellerImage: ImageView? = null
    private var title: TextView? = null
    private var price: TextView? = null
    private var gender: TextView? = null
    private var category: TextView? = null
    private var condition: TextView? = null
    private var description: TextView? = null
    var sellerName: TextView? = null
    private var editButton: FloatingActionButton? = null
    private var deleteButton: FloatingActionButton? = null
    var progressBar: ProgressBar? = null
    var view: View? = null
    private var removeFromFavorites: ImageView? = null
    private var addToFavorite: ImageView? = null
    private var viewModel: FavoriteProductListRvViewModel? = null
    private var cbIsSold: CheckBox? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(this)[FavoriteProductListRvViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.fragment_product_details, container, false)
        val productId: String = ProductDetailsFragmentArgs.fromBundle(arguments).getProductId()
        attachFragmentElement(view)
        Model.instance.getProductById(productId
        ) { product ->
            if (product != null) {
                setFragmentElements(product)
            }
            if (product != null) {
                displayOwnerButtons(product, productId)
            }
            if (product != null) {
                getProductSeller(product)
            }
            if (product != null) {
                favoritesHandler(product, productId)
            }
        }
        return view
    }

    private fun favoritesHandler(product: Product, productId: String) {
        viewModel?.getData()?.observe(viewLifecycleOwner) { showFavoritesIcon(product) }
        if (!product.contactId?.let { isCurrentUserProduct(it) }!!) {
            addToFavorite = requireView().findViewById(R.id.add_to_favorite)
            removeFromFavorites = requireView().findViewById(R.id.in_favorite_icon)
            handleRemoveFromFavorites(productId)
            handleAddToFavorites(productId)
        }
    }

    private fun handleRemoveFromFavorites(productId: String) {
        removeFromFavorites?.setOnClickListener {
            if (removeFromFavorites?.visibility == View.VISIBLE) {
                removeFromFavorites?.visibility = View.GONE
                addToFavorite?.visibility = View.VISIBLE
            }
            Model.instance.removeFromLikedProducts(productId, object : RemoveLikedProductsListener {
                override fun onComplete() {
                    println("Product removal completed")
                }
            })
        }
    }


    private fun handleAddToFavorites(productId: String) {
        addToFavorite?.setOnClickListener {
            if (addToFavorite?.visibility == View.VISIBLE) {
                removeFromFavorites?.visibility = View.VISIBLE
                addToFavorite?.visibility = View.GONE
            }
            Model.instance.addToLikedProducts(productId, object : AddLikedProductListener {
                override fun onComplete() {
                    println("Product addition to favorites completed")
                }
            })
        }
    }

    private fun isCurrentUserProduct(contactId: String): Boolean {
        return Model.instance.mAuth.uid.equals(contactId)
    }

    open fun getProductSeller(product: Product) {
        product.contactId?.let {
            Model.instance.getProductSellerUser(it, object : Model.GetLoggedUserListener {
                @SuppressLint("SetTextI18n")
                override fun onComplete(user: User) {
                    progressBar?.visibility = View.GONE
                    if (user.userImageUrl != null) {
                        Picasso.get()
                            .load(user.userImageUrl)
                            .into(sellerImage)
                    } else {
                        sellerImage?.setImageResource(R.drawable.no_product_image)
                    }
                    sellerName?.text = "${user.firstName} ${user.lastName}"
                    sellerName?.setOnClickListener {
                        navigateToUserProfile(
                            user,
                            product.contactId!!
                        )
                    }
                    sellerImage?.setOnClickListener {
                        navigateToUserProfile(
                            user,
                            product.contactId!!
                        )
                    }
                }
            })
        }
    }

    private fun displayOwnerButtons(product: Product, productId: String) {
        if (product.contactId?.let { isCurrentUserProduct(it) } == true) {
            displayEditButton(productId)
            displayDeleteButton(product)
            displayCheckIsSoldButton(product)
            removeFromFavorites!!.visibility = View.GONE
            addToFavorite!!.visibility = View.GONE
        }
    }

    private fun displayDeleteButton(product: Product) {
        deleteButton!!.visibility = View.VISIBLE
        deleteButton!!.setOnClickListener { openDeleteDialog(product) }
    }

    private fun displayCheckIsSoldButton(product: Product) {
        cbIsSold!!.isEnabled = true
        cbIsSold!!.setOnCheckedChangeListener { _, isChecked ->
            product.isSold = isChecked
            Model.instance.saveProduct(product) { Log.d("Products", "Product updated") }
        }
    }

    private fun displayEditButton(productId: String) {
        editButton!!.visibility = View.VISIBLE
        editButton!!.setOnClickListener { v ->
            findNavController(v).navigate(
                ProductDetailsFragmentDirections.navProductDetailsToNavAddProduct(
                    productId
                )
            )
        }
    }

    private fun setFragmentElements(product: Product) {
        title?.text = product.title
        val productPrice: String? = product.price
        price!!.text =
            if (productPrice != null) MessageFormat.format("{0}$", product.price) else ""
        gender?.text = product.gender
        category?.text  = product.productCategory
        condition?.text = product.productCondition
        description?.text = product.description
        if (product.imageUrl != null) {
            Picasso.get().load(product.imageUrl).into(imageUrl)
        }
        cbIsSold!!.isChecked = product.isSold
    }

    private fun attachFragmentElement(view: View?) {
        imageUrl = requireView().findViewById(R.id.detailsProductImage)
        title = view?.findViewById(R.id.detailsProductTitle)
        price = view?.findViewById(R.id.detailsProductPrice)
        gender = view?.findViewById(R.id.detailProductGender)
        category = view?.findViewById(R.id.detailProductCategory)
        condition = view?.findViewById(R.id.detailsProductCondition)
        description = view?.findViewById(R.id.detailsProductDescription)
        editButton = view?.findViewById(R.id.floatingEditButton)
        deleteButton = view?.findViewById(R.id.floatingDeleteButton)
        cbIsSold = view?.findViewById(R.id.detailsIsSoldCheckBox)
        sellerImage = view?.findViewById(R.id.seller_image)
        sellerName = view?.findViewById(R.id.seller_name)
        progressBar = view?.findViewById(R.id.products_details_progress_bar)
        addToFavorite = view?.findViewById(R.id.add_to_favorite)
        removeFromFavorites = view?.findViewById(R.id.in_favorite_icon)
    }

    private fun openDeleteDialog(product: Product) {
        val alert = AlertDialog.Builder(activity)
        alert.setTitle("Do you want to delete this product?")
        alert.setPositiveButton("Yes"
        ) { _, _ ->
            product.isDeleted = true
            Model.instance.saveProduct(product) {
                findNavController((title)!!).navigateUp()
            }
        }
        alert.setNegativeButton("Cancel") { _, _ -> }
        alert.show()
    }

    private fun navigateToUserProfile(user: User, contactId: String) {
        val bundle = Bundle()
        bundle.putSerializable("user", user)
        val userProfileFragment = UserProfileFragment()
        userProfileFragment.arguments = bundle
        if (Model.instance.mAuth.uid.equals(contactId)) {
            findNavController(requireView()).navigate(R.id.to_nav_user_profile, bundle)
        } else {
            findNavController(requireView()).navigate(R.id.to_other_user_profile, bundle)
        }
    }

    private fun showFavoritesIcon(product: Product) {
        if (!product.contactId?.let { isCurrentUserProduct(it) }!!) {
            val favoritesProducts: List<String> = viewModel?.getData()?.value
                ?.map { it.id }
                ?: emptyList()
            if (favoritesProducts.contains(product.id)) {
                removeFromFavorites!!.visibility = View.VISIBLE
                addToFavorite!!.visibility = View.GONE
            } else {
                addToFavorite!!.visibility = View.VISIBLE
                removeFromFavorites!!.visibility = View.GONE
            }
        }
    }
}