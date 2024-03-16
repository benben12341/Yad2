package com.example.yad2.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yad2.viewModels.ProductListRvViewModel
import com.example.yad2.R
import com.example.yad2.interfaces.OnItemClickListener
import com.example.yad2.models.cb.ProductFilterCB

class FilterFragment : Fragment() {
    var types: List<ProductFilterCB>? = null
    var adapter: MyProductTypesAdapter? = null
    var selectedStrings: MutableList<String> = ArrayList()
    var viewModel: ProductListRvViewModel? = null
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
        val view: View = inflater.inflate(R.layout.filter_fragment, container, false)
        types = ProductFilterCB.allCheckboxTypes
        val list: RecyclerView = view.findViewById<RecyclerView>(R.id.itemslist_rv)
        list.setHasFixedSize(true)
        list.setLayoutManager(LinearLayoutManager(context))
        adapter = MyProductTypesAdapter()
        list.setAdapter(adapter)
        viewModel?.addTypeFilter(ArrayList<String>())
        selectedStrings = ArrayList()
        viewModel?.addTypeFilter(selectedStrings)
        return view
    }

    inner class MyViewHolder(itemView: View, listener: OnItemClickListener?) :
        RecyclerView.ViewHolder(itemView) {
        var cb: CheckBox

        init {
            cb = itemView.findViewById<CheckBox>(R.id.type_product_filter_cb)
            cb.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
                override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
                    if (isChecked) {
                        selectedStrings.add(cb.getText().toString())
                    } else {
                        selectedStrings.remove(cb.getText().toString())
                    }
                    viewModel?.addTypeFilter(selectedStrings)
                }
            })
            itemView.setOnClickListener { v ->
                val pos: Int = getAdapterPosition()
                listener?.onItemClick(v, pos)
            }
        }
    }
    inner class MyProductTypesAdapter : RecyclerView.Adapter<MyViewHolder?>() {
        var listener: OnItemClickListener? = null
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): MyViewHolder {
            val view: View =
                layoutInflater.inflate(R.layout.product_type_checkbox, parent, false)
            return MyViewHolder(view, listener)
        }

        override fun getItemCount(): Int {
            return types!!.size
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val productFilterCB: ProductFilterCB = types!![position]
            holder.cb.setChecked(productFilterCB.isFlag)
            holder.cb.setText(productFilterCB.productType)
        }
    }
}
