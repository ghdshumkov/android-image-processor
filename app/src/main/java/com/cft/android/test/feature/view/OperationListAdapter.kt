package com.cft.android.test.feature.view

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.cft.android.test.R
import com.cft.android.test.feature.model.operation.OperationStatusObserver
import com.cft.android.test.feature.model.StatusProgress
import com.cft.android.test.feature.model.StatusResult
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy


/**
 * Created by administrator <dshumkov@icerockdev.com> on 20.10.18.
 */
class OperationListAdapter(private val mItemListener: (Int) -> Unit) : RecyclerView.Adapter<OperationStatusViewHolder>() {

    var statusList: List<OperationStatusObserver> = ArrayList()
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return statusList.size
    }

    override fun onViewRecycled(holder: OperationStatusViewHolder) {
        super.onViewRecycled(holder)

        Log.d("onViewRecycled: %s", "")
        holder.disposable?.dispose()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OperationStatusViewHolder {
        return OperationStatusViewHolder(parent, mItemListener)
    }

    override fun onBindViewHolder(holder: OperationStatusViewHolder, position: Int) {

        val color = if (position%2 == 0)
            ContextCompat.getColor(holder.itemView.context, R.color.colorPrimaryDark) else ContextCompat.getColor(holder.itemView.context, R.color.colorPrimary)
        holder.binding.root.setBackgroundColor(color)

        holder.disposable = statusList[position].operationStatus()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    holder.binding.progress.visibility = View.GONE
                    holder.binding.resultImage.visibility = View.INVISIBLE
                    holder.binding.error.visibility = View.GONE
                }
                .subscribeBy(
                    onNext = {
                    when (it) {
                        is StatusProgress -> {
                            Log.d("Holder progress: %s", it.progress.toString())
                            holder.binding.progress.visibility = View.VISIBLE
                            holder.binding.progress.progress = it.progress
                        }
                        is StatusResult -> {
                            Log.d("Holder result: %s", it.uri.toString())
                            Picasso.get()
                                    .load(it.uri)
                                    .resize(400, 400)
                                    .centerCrop()
                                    .into(holder.binding.resultImage)

                            Log.d("Holder result: %s", "GONE")
                        }
                    }
                },
                    onComplete = {
                        holder.binding.progress.visibility = View.GONE
                        holder.binding.resultImage.visibility = View.VISIBLE
                        Log.d("Holder result: %s", "OnComplete")
                },
                    onError = {
                        holder.binding.progress.visibility = View.GONE
                        holder.binding.resultImage.visibility = View.INVISIBLE
                        holder.binding.error.visibility = View.VISIBLE
                        holder.binding.error.text = it.localizedMessage
                        Log.e("Holder result: %s", "Error", it)
                }
        )
    }
}