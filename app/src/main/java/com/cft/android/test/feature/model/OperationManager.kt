package com.cft.android.test.feature.model

import android.content.Context
import android.net.Uri
import com.cft.android.test.feature.model.operation.*
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import java.util.*

class OperationManager(private val context: Context) : IOperationManager {

    override val operations = ArrayList<Operation>()

    private var mCurrentNotListedOperation: DownloadOperation? = null

    override fun newRotateOperation(input: Uri): Int {
        val operation = RotateOperation(context, input)
        operation.execute()
        operations.add(operation)
        return operations.indexOf(operation)
    }

    override fun newGrayscaleOperation(input: Uri): Int {
        val operation = GrayscaleOperation(context, input)
        operation.execute()
        operations.add(operation)
        return operations.indexOf(operation)
    }

    override fun newFlipOperation(input: Uri): Int {
        val operation = FlipOperation(context, input)
        operation.execute()
        operations.add(operation)
        return operations.indexOf(operation)
    }

    override fun newDownloadOperation(url: String): Boolean {
        if (mCurrentNotListedOperation != null)
            return false

        val operation = DownloadOperation(context, url)
        operation.execute()
        mCurrentNotListedOperation = operation
        return true
    }

    override fun getCurrentDownload(): Flowable<OperationStatus>? {
        return mCurrentNotListedOperation?.operationStatus()
                ?.toFlowable(BackpressureStrategy.BUFFER)
    }

    override fun removeCurrentDownload() {
        mCurrentNotListedOperation = null
    }
}