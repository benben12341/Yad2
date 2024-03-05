package com.example.yad2.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import com.example.yad2.interfaces.GetProductByIdListener
import com.example.yad2.models.Model
import com.example.yad2.models.Product
import com.example.yad2.models.User
import com.example.yad2.viewModels.FavoriteProductListRvViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso
import java.text.MessageFormat
import com.example.yad2.R

class ProductDetailsFragment() : Fragment() {
    var imageUrl: ImageView? = null
    var sellerImage: ImageView? = null
    var title: TextView? = null
    var price: TextView? = null
    var gender: TextView? = null
    var category: TextView? = null
    var condition: TextView? = null
    var description: TextView? = null
    var sellerName: TextView? = null
    var editButton: FloatingActionButton? = null
    var deleteButton: FloatingActionButton? = null
    var progressBar: ProgressBar? = null
    var view: View? = null
    var removeFromFavorites: ImageView? = null
    var addToFavorite: ImageView? = null
    var viewModel: FavoriteProductListRvViewModel? = null
    var cbIsSold: CheckBox? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(this).get(FavoriteProductListRvViewModel::class.java)
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        view = inflater.inflate(R.layout.fragment_product_details, container, false)
        val productId: String = ProductDetailsFragmentArgs.fromBundle(arguments).getProductId()
        attachFragmentElement(view)
        Model.instance.getProductById(productId, object : GetProductByIdListener {
            @RequiresApi(api = Build.VERSION_CODES.N)
            override fun onComplete(product: Product) {
                setFragmentElements(product)
                displayOwnerButtons(product, productId)
                getProductSeller(product)
                FavoritesHandler(product, productId)
            }
        })
        return view
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun FavoritesHandler(product: Product, productId: String) {
        viewModel?.getData()?.observe(viewLifecycleOwner) { showFavoritesIcon(product) }
        if (!product.contactId?.let { isCurrentUserProduct(it) }!!) {
            addToFavorite = requireView().findViewById<ImageView>(R.id.add_to_favorite)
            removeFromFavorites = requireView().findViewById<ImageView>(R.id.in_favorite_icon)
            HandleRemoveFromFavorites(productId)
            HandleAddToFavorites(productId)
        }
    }

    private fun HandleRemoveFromFavorites(productId: String) {
        removeFromFavorites!!.setOnClickListener(View.OnClickListener {
            if (removeFromFavorites!!.visibility == View.VISIBLE) {
                removeFromFavorites!!.visibility = View.GONE
                addToFavorite!!.visibility = View.VISIBLE
            }
            Model.instance.removeFromLikedProducts(productId) {}
        })
    }

    private fun HandleAddToFavorites(productId: String) {
        addToFavorite!!.setOnClickListener(object : View.OnClickListener {
            @RequiresApi(api = Build.VERSION_CODES.N)
            override fun onClick(v: View) {
                if (addToFavorite!!.visibility == View.VISIBLE) {
                    removeFromFavorites!!.visibility = View.VISIBLE
                    addToFavorite!!.visibility = View.GONE
                }
                Model.instance.addToLikedProducts(productId) {}
            }
        })
    }

    private fun isCurrentUserProduct(contactId: String): Boolean {
        return Model.instance.mAuth.getUid().equals(contactId)
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun getProductSeller(product: Product) {
        product.contactId?.let {
            Model.instance.getProductSellerUser(it) { user: User ->
                progressBar!!.setVisibility(View.GONE)
                if (user != null) {
                    if (user.userImageUrl != null) {
                        Picasso.get()
                            .load(user.userImageUrl)
                            .into(sellerImage)
                    } else {
                        sellerImage!!.setImageResource(R.drawable.no_product_image)
                    }
                    sellerName?.setText(user.firstName + " " + user.lastName)
                    sellerName!!.setOnClickListener(object : View.OnClickListener {
                        override fun onClick(view: View) {
                            navigateToUserProfile(user, product.contactId!!)
                        }
                    })
                    sellerImage!!.setOnClickListener(object : View.OnClickListener {
                        override fun onClick(view: View) {
                            navigateToUserProfile(user, product.contactId!!)
                        }
                    })
                } else {
                    sellerImage!!.setVisibility(View.GONE)
                    sellerName!!.setText("seller not found ):")
                }
            }
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
        deleteButton!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                openDeleteDialog(product)
            }
        })
    }

    private fun displayCheckIsSoldButton(product: Product) {
        cbIsSold!!.isEnabled = true
        cbIsSold!!.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
                product.isSold = isChecked
                Model.instance.saveProduct(product) { isSuccess: Boolean ->
                    if (isSuccess) {
                        Log.d("Products", "Product updated successfully")
                    } else {
                        Log.d("Products", "Failed to update product")
                    }
                }
            }
        })
    }

    private fun displayEditButton(productId: String) {
        editButton!!.visibility = View.VISIBLE
        editButton!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                findNavController(v).navigate(
                    ProductDetailsFragmentDirections.navProductDetailsToNavAddProduct(
                        productId
                    )
                )
            }
        })
    }

    private fun setFragmentElements(product: Product) {
        title?.setText(product.title)
        val productPrice: String? = product.price
        price!!.text =
            if (productPrice != null) MessageFormat.format("{0}$", product.price) else ""
        gender?.setText(product.gender)
        category?.text  = product.productCategory
        condition?.text = product.productCondition
        description?.text = product.description
        if (product.imageUrl != null) {
            Picasso.get().load(product.imageUrl).into(imageUrl)
        }
        cbIsSold!!.isChecked = product.isSold
    }

    private fun attachFragmentElement(view: View?) {
        imageUrl = requireView().findViewById<ImageView>(R.id.detailsProductImage)
        title = view?.findViewById<TextView>(R.id.detailsProductTitle)
        price = view?.findViewById<TextView>(R.id.detailsProductPrice)
        gender = view?.findViewById<TextView>(R.id.detailProductGender)
        category = view?.findViewById<TextView>(R.id.detailProductCategory)
        condition = view?.findViewById<TextView>(R.id.detailsProductCondition)
        description = view?.findViewById<TextView>(R.id.detailsProductDescription)
        editButton = view?.findViewById<FloatingActionButton>(R.id.floatingEditButton)
        deleteButton = view?.findViewById<FloatingActionButton>(R.id.floatingDeleteButton)
        cbIsSold = view?.findViewById<CheckBox>(R.id.detailsIsSoldCheckBox)
        sellerImage = view?.findViewById<ImageView>(R.id.seller_image)
        sellerName = view?.findViewById<TextView>(R.id.seller_name)
        progressBar = view?.findViewById<ProgressBar>(R.id.products_details_progress_bar)
        addToFavorite = view?.findViewById<ImageView>(R.id.add_to_favorite)
        removeFromFavorites = view?.findViewById<ImageView>(R.id.in_favorite_icon)
    }

    private fun openDeleteDialog(product: Product) {
        val alert = AlertDialog.Builder(activity)
        alert.setTitle("Do you want to delete this product?")
        alert.setPositiveButton("Yes", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, whichButton: Int) {
                product.isDeleted = true
                Model.instance.saveProduct(product) {
                    findNavController((title)!!).navigateUp() }
            }
        })
        alert.setNegativeButton("Cancel",
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, whichButton: Int) {}
            })
        alert.show()
    }

    private fun navigateToUserProfile(user: User, contactId: String) {
        val bundle = Bundle()
        bundle.putSerializable("user", user)
        val userProfileFragment = UserProfileFragment()
        userProfileFragment.arguments = bundle
        if (Model.instance.mAuth.getUid().equals(contactId)) {
            findNavController((view)!!).navigate(R.id.to_nav_user_profile, bundle)
        } else {
            findNavController((view)!!).navigate(R.id.to_other_user_profile, bundle)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
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