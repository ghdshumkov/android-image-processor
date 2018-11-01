package com.cft.android.test.feature.view

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.cft.android.test.R
import com.cft.android.test.databinding.ItemOperationStatusBinding
import io.reactivex.disposables.Disposable


/**
 * Created by administrator <dshumkov@icerockdev.com> on 20.10.18.
 */
open class BindingViewHolder<T : ViewDataBinding>(val binding: T) : RecyclerView.ViewHolder(binding.root) {

    constructor(parent: ViewGroup, @LayoutRes layoutId: Int) :
        this(DataBindingUtil.inflate<T>(LayoutInflater.from(parent.context), layoutId, parent, false))
}

class OperationStatusViewHolder(parent: ViewGroup, itemListener: (Int) -> Unit) : BindingViewHolder<ItemOperationStatusBinding>(parent, R.layout.item_operation_status) {

    var disposable: Disposable? = null

    init {
        binding.root.setOnClickListener {
            if (adapterPosition != RecyclerView.NO_POSITION) {
                itemListener(adapterPosition)
            }
        }
    }
}