

package com.example.yad2.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
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
import com.example.yad2.viewModels.UserProductsListRvViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class UserProductsFragment : Fragment() {
    private var viewModel: UserProductsListRvViewModel? = null
    private var adapter: MyAdapter? = null
    private var swipeRefresh: SwipeRefreshLayout? = null
    private var progressBar: ProgressBar? = null
    private var userId: String? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(this).get(UserProductsListRvViewModel::class.java)
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        userId = Model.instance.mAuth.currentUser?.uid
        val view: View = inflater.inflate(R.layout.fragment_my_products, container, false)
        val list: RecyclerView = view.findViewById<RecyclerView>(R.id.my_products_itemslist_rv)
        progressBar = view.findViewById<ProgressBar>(R.id.products_user_progress_bar)
        progressBar?.setVisibility(View.GONE)
        swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.myproductslist_swiperefresh)
        swipeRefresh?.setOnRefreshListener { Model.instance.refreshProductsByMyUser() }
        viewModel.refreshUserItems()
        val add: FloatingActionButton =
            view.findViewById(R.id.addProductButton)
        add.setOnClickListener { v ->
            findNavController(v)
                .navigate(MyProductsFragmentDirections.navMyProductsToNavAddProduct(null))
        }
        list.setHasFixedSize(true)
        list.layoutManager = LinearLayoutManager(context)
        adapter = MyAdapter()
        list.setAdapter(adapter)
        setHasOptionsMenu(true)
        adapter!!.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(v: View?, position: Int) {
                val stId: String = viewModel?.getData()?.value?.get(position)?.id.toString()
                findNavController(v!!).navigate(
                    UserProductsFragmentDirections.navMyProductsToNavProductDetails(
                        stId
                    )
                )
            }
        })
        viewModel?.getData()?.observe(viewLifecycleOwner) { list1 -> refresh() }
        swipeRefresh?.setRefreshing(
            Model.instance.productListLoadingState.value
                    === Model.ProductsListLoadingState.LOADING
        )
        Model.instance.userProductsLoadingState
            .observe(viewLifecycleOwner) { userItemLoadingState ->
                if (userItemLoadingState === Model.ProductsListLoadingState.LOADING) {
                    progressBar?.visibility=(View.VISIBLE)
                    progressBar?.visibility=(View.VISIBLE)
                } else {
                    progressBar?.visibility=(View.GONE)
                    progressBar?.visibility=(View.GONE)
                }
            }
        return view
    }

    private fun refresh() {
        adapter?.notifyDataSetChanged()
        swipeRefresh?.isRefreshing = false
    }

    inner class MyAdapter : RecyclerView.Adapter<CardViewHolder?>() {
        var listener: OnItemClickListener? = null
        fun setOnItemClickListener(listener: OnItemClickListener?) {
            this.listener = listener
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): CardViewHolder {
            val view: View = layoutInflater.inflate(R.layout.item_card, parent, false)
            return CardViewHolder(view, listener, context)
        }

        // Replace the contents of a view (invoked by the layout manager)
        @RequiresApi(api = Build.VERSION_CODES.N)
        override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
            val product: Product = viewModel.getData().getValue().get(position)
            holder.bind(product)
        }

        override fun getItemCount(): Int {
            if (viewModel?.getData()?.value == null) return 0
            else return viewModel?.getData()?.value?.size!!.toInt()
        }
    }
}