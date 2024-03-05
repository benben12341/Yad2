package com.example.yad2.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.ScrollView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.yad2.R
import com.example.yad2.models.Model
import com.example.yad2.models.Model.ProductsListLoadingState
import com.example.yad2.models.Product
import com.example.yad2.models.ProductListRvViewModel
import com.example.yad2.shared.CardViewHolder
import com.example.yad2.shared.OnItemClickListener
import com.example.yad2.HomeFragmentDirections

class HomeFragment : Fragment() {
    var viewModel: ProductListRvViewModel? = null
    var adapter: MyAdapter? = null
    var swipeRefresh: SwipeRefreshLayout? = null
    private var progressBar: ProgressBar? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel =
            ViewModelProvider(this).get<ProductListRvViewModel>(ProductListRvViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_home, container, false)
        swipeRefresh = view.findViewById(R.id.productslist_swiperefresh)
        swipeRefresh?.setOnRefreshListener { Model.instance.refreshProductsList() }
        progressBar = view.findViewById<ProgressBar>(R.id.products_home_progress_bar)
        progressBar?.setVisibility(View.GONE)
        viewModel?.refreshProducts()
        val productList: RecyclerView = view.findViewById<RecyclerView>(R.id.productslist_rv)
        val categoryFilterButton = view.findViewById<Button>(R.id.category_filter_bv)
        productList.setHasFixedSize(true)
        productList.layoutManager = LinearLayoutManager(context)
        adapter = MyAdapter()
        productList.setAdapter(adapter)
        setHasOptionsMenu(true)
        adapter!!.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(v: View?, position: Int) {
                val productId: String = viewModel?.data?.value?.get(position)?.id.toString()
                findNavController(v).navigate(
                    HomeFragmentDirections.navHomeToNavProductDetails(
                        productId
                    )
                )
            }
        })
        ToggleCategoriesFilter(view, categoryFilterButton)
        viewModel?.data?.observe(
            viewLifecycleOwner,
            Observer<List<Product?>> { list1: List<Product?>? -> refresh() })
        Model.instance.productListLoadingState
            .observe(viewLifecycleOwner) { productsListLoadingState ->
                if (productsListLoadingState === ProductsListLoadingState.LOADING) {
                    progressBar?.visibility = View.VISIBLE
                    progressBar?.visibility = View.VISIBLE
                } else {
                    progressBar?.visibility = View.GONE
                    progressBar?.visibility = View.GONE
                }
            }
        return view
    }

    private fun ToggleCategoriesFilter(view: View, categoryFilterButton: Button) {
        categoryFilterButton.setOnClickListener {
            val typeScrollView: ScrollView =
                view.findViewById(R.id.type_filters_scroll_view)
            typeScrollView.setVisibility(if (typeScrollView.getVisibility() == View.GONE) View.VISIBLE else View.GONE)
        }
    }

    private fun refresh() {
        adapter?.notifyDataSetChanged()
        swipeRefresh?.isRefreshing = false
    }

    inner class MyAdapter : RecyclerView.Adapter<CardViewHolder?>() {
        var listener: OnItemClickListener? = null
        fun setOnItemClickListener(listener: OnItemClickListener?) {
            if (listener != null) {
                this.listener = listener
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): CardViewHolder {
            val view: View = layoutInflater.inflate(R.layout.item_card, parent, false)
            return CardViewHolder(view, listener!!, context!!)
        }

        override fun getItemCount(): Int {
            if (viewModel?.data?.value == null) return 0
            else return viewModel?.data?.value?.size!!.toInt()
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
            val product: Product? = viewModel?.data?.value?.get(position)
            holder.bind(product)
        }
    }
}