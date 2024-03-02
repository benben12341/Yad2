package com.example.yad2.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import com.example.yad2.R
import com.example.yad2.models.User
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso

class UserProfileFragment : Fragment() {
    var email: TextView? = null
    var phone: TextView? = null
    var address: TextView? = null
    var fullUserName: TextView? = null
    var productsTitle: TextView? = null
    var favoritesTitle: TextView? = null
    var userFavorites: ImageButton? = null
    var userProducts: ImageButton? = null
    var editDetails: ImageButton? = null
    var userImage: ImageView? = null
    var currentUser: User? = null
    var mAuth: FirebaseAuth? = null
    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_user_profile, container, false)
        fullUserName = view.findViewById<TextView>(R.id.full_user_name)
        email = view.findViewById<TextView>(R.id.email_txt)
        phone = view.findViewById<TextView>(R.id.phone_txt)
        address = view.findViewById<TextView>(R.id.address_txt)
        userFavorites = view.findViewById<ImageButton>(R.id.user_favorites)
        userProducts = view.findViewById<ImageButton>(R.id.user_products)
        userImage = view.findViewById<ImageView>(R.id.user_image)
        editDetails = view.findViewById<ImageButton>(R.id.edit_details)
        productsTitle = view.findViewById<TextView>(R.id.products_title)
        favoritesTitle = view.findViewById<TextView>(R.id.favorites_title)
        val bundle = this.arguments
        if (bundle != null) {
            currentUser = bundle.getSerializable("user") as User?
        }
        email?.setText(currentUser!!.email)
        phone?.setText(currentUser!!.phoneNumber)
        address?.setText(currentUser!!.address)
        fullUserName?.setText(currentUser!!.firstName + " " + currentUser!!.lastName)
        if (currentUser!!.userImageUrl != null) {
            Picasso.get()
                .load(currentUser!!.userImageUrl)
                .into(userImage)
        }
        mAuth = FirebaseAuth.getInstance()


        // show edit button only if the user is logged user
        val visibility = if (currentUser?.id
                .equals(mAuth!!.getCurrentUser()?.getUid())
        ) View.VISIBLE else View.INVISIBLE
        editDetails?.setVisibility(visibility)
        userFavorites?.setVisibility(visibility)
        userProducts?.setVisibility(visibility)
        productsTitle?.setVisibility(visibility)
        favoritesTitle?.setVisibility(visibility)
        userFavorites?.setOnClickListener(View.OnClickListener { view ->
            findNavController(view).navigate(
                R.id.to_nav_favorites
            )
        })
        userProducts?.setOnClickListener(View.OnClickListener { view ->
            findNavController(view).navigate(
                R.id.to_nav_my_products
            )
        })
        editDetails?.setOnClickListener(View.OnClickListener { view ->
            val bundle = Bundle()
            bundle.putSerializable("user", currentUser)
            val editDetailsFragment = EditUserDetailsFragment()
            editDetailsFragment.setArguments(bundle)
            findNavController(view).navigate(R.id.to_edit_user_details, bundle)
        })
        return view
    }
}