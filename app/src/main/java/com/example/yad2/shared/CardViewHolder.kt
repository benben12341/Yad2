package com.example.yad2.shared

import android.content.Context
import android.os.Build
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.squareup.picasso.Picasso
import com.example.yad2.R
import com.example.yad2.models.Model
import com.example.yad2.models.Product
import com.example.yad2.models.User

class CardViewHolder(itemView: View, listener: OnItemClickListener, context: Context) :
    RecyclerView.ViewHolder(itemView) {
    var itemImage: ImageView? = null
    var contactImage: ImageView? = null
    var title: TextView? = null
    var date: TextView? = null
    var price: TextView? = null
    var isSold: TextView? = null
    private val context: Context

    init {
        getProductCardElements(itemView)
        this.context = context
        itemView.setOnClickListener { v ->
            val pos: Int = getAdapterPosition()
            listener.onItemClick(v, pos)
        }
    }

    private fun getProductCardElements(itemView: View) {
        itemImage = itemView.findViewById<ImageView>(R.id.image_item)
        contactImage = itemView.findViewById<ImageView>(R.id.contact_image)
        title = itemView.findViewById<TextView>(R.id.item_title_tv)
        price = itemView.findViewById<TextView>(R.id.contact_location_tv)
        date = itemView.findViewById<TextView>(R.id.upload_product_date_tv)
        isSold = itemView.findViewById<TextView>(R.id.item_sold)
        contactImage?.setImageResource(R.drawable.no_person_image)
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    fun bind(product: Product) {
        title?.setText(product.title)
        price!!.text =
            if (product.price != null) product.price + "$" else "Price not listed"
        itemImage!!.setImageResource(R.drawable.no_product_image)
        isSold!!.visibility =
            if (product.isSold) View.VISIBLE else View.INVISIBLE
        date!!.text = UtilFunctions.getDate(product.updateDate)
        if (product.imageUrl != null) {
            Picasso.get()
                .load(product.imageUrl)
                .into(itemImage)
        } else {
            itemImage!!.setImageResource(R.drawable.no_product_image)
        }
        Model.instance.getUser(product.contactId!!, object : Model.GetLoggedUserListener {
            override fun onComplete(user: User) {
                if (user != null && user.userImageUrl != null) {
                    // Load the user's image using Glide
                    Glide.with(context)
                        .load(user.userImageUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .into(contactImage!!)
        }
    }})
    }
}