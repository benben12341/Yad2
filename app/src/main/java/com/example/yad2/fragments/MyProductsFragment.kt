package com.example.yad2.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.yad2.R
import com.example.yad2.models.Model
import com.example.yad2.models.Product
import com.example.yad2.shared.CardViewHolder
import com.example.yad2.shared.OnItemClickListener
import com.example.yad2.viewModels.FavoriteProductListRvViewModel

class MyProductsFragment : Fragment() {
    var viewModel: FavoriteProductListRvViewModel? = null
    private var adapter: MyAdapter? = null
    private var swipeRefresh: SwipeRefreshLayout? = null
    private var progressBar: ProgressBar? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(this)[FavoriteProductListRvViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_liked_products, container, false)
        val list: RecyclerView = view.findViewById(R.id.like_products_itemslist_rv)
        progressBar = view.findViewById(R.id.favorite_products_progress_bar)
        progressBar?.visibility = View.GONE
        view.findViewById<SwipeRefreshLayout>(R.id.likedproductslist_swiperefresh).also { swipeRefresh = it }
        swipeRefresh?.setOnRefreshListener { Model.instance.refreshProductsILikedByUserList() }
        viewModel?.refreshFavoriteItems()
        list.setHasFixedSize(true)
        list.layoutManager = LinearLayoutManager(context)
        adapter = MyAdapter()
        list.adapter = adapter
        setHasOptionsMenu(true)
        viewModel?.data?.observe(
            viewLifecycleOwner
        ) { refresh() }
        adapter!!.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(v: View?, position: Int) {
                val stId: String = viewModel?.data?.value?.get(position)!!.id
                if (v != null) {
                    findNavController(v).navigate(
                        MyProductsFragmentDirections.navLikedProductsToNavProductDetails(
                            stId
                        )
                    )
                }
            }
        })
        Model.instance.favoriteProductsLoadingState
            .observe(viewLifecycleOwner) { favoriteLoadingState ->
                if (favoriteLoadingState === Model.ProductsListLoadingState.LOADING) {
                    progressBar?.visibility = View.VISIBLE
                    progressBar?.visibility = View.VISIBLE
                } else {
                    progressBar?.visibility = View.GONE
                    progressBar?.visibility = View.GONE
                }
            }
        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun refresh() {
        adapter!!.notifyDataSetChanged()
        swipeRefresh?.isRefreshing = false
    }


   inner class MyAdapter : RecyclerView.Adapter<CardViewHolder>() {
        var listener: OnItemClickListener? = null
        fun setOnItemClickListener(listener: OnItemClickListener?) {
            this.listener = listener
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): CardViewHolder {
            val view: View =
                layoutInflater.inflate(R.layout.item_card, parent, false)
            return CardViewHolder(view, listener!!, context!!)
        }

        override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
            val product: Product = viewModel?.data?.value?.get(position)!!
            holder.bind(product)
        }

        override fun getItemCount(): Int {
            return if (viewModel?.data?.value == null) {
                0
            } else viewModel?.data?.value?.size!!.toInt()
        }
    }

}