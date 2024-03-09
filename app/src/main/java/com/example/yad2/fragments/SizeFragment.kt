package com.example.yad2.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yad2.R
import com.example.yad2.models.cb.ProductSizeCB
import com.example.yad2.viewModels.ProductListRvViewModel

class SizeFragment : Fragment() {
    var types: List<ProductSizeCB>? = null
    var adapter: MyProductTypesAdapter? = null
    var selectedStrings: MutableList<String> = ArrayList()
    var viewModel: ProductListRvViewModel? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(this).get(ProductListRvViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.filter_fragment, container, false)
        types = ProductSizeCB.allCheckboxSizes
        val list = view.findViewById<RecyclerView>(R.id.itemslist_rv)
        list.setHasFixedSize(true)
        list.layoutManager = LinearLayoutManager(context)
        adapter = MyProductTypesAdapter()
        list.adapter = adapter
        return view
    }

    inner class MyViewHolder(itemView: View, listener: OnItemClickListener?) :
        RecyclerView.ViewHolder(itemView) {
        var cb: CheckBox

        init {
            cb = itemView.findViewById<CheckBox>(R.id.type_product_filter_cb)
            cb.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    selectedStrings.add(cb.text.toString())
                } else {
                    selectedStrings.remove(cb.text.toString())
                }
            }
            itemView.setOnClickListener { v ->
                val pos = adapterPosition
                listener!!.onItemClick(v, pos)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(v: View?, position: Int)
    }

    inner class MyProductTypesAdapter : RecyclerView.Adapter<MyViewHolder>() {
        var listener: OnItemClickListener? = null
        fun setOnItemClickListener(listener: OnItemClickListener?) {
            this.listener = listener
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): MyViewHolder {
            val view: View =
                layoutInflater.inflate(R.layout.product_type_checkbox, parent, false)
            return MyViewHolder(view, listener)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val productSizeFilterCB = types!![position]
            holder.cb.isChecked = productSizeFilterCB.isFlag
            holder.cb.text = productSizeFilterCB?.productSize
        }

        override fun getItemCount(): Int {
            return types!!.size
        }
    }
}
