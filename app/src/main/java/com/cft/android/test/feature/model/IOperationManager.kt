package com.cft.android.test.feature.model

import android.net.Uri
import com.cft.android.test.feature.model.operation.Operation
import io.reactivex.Flowable
import io.reactivex.Observable
import java.util.*


/**
 * Created by administrator <dshumkov@icerockdev.com> on 26.10.18.
 */
interface IOperationManager {

    val operations : ArrayList<Operation>

    fun newRotateOperation(input: Uri): Int

    fun newGrayscaleOperation(input: Uri): Int

    fun newFlipOperation(input: Uri): Int

    fun newDownloadOperation(url: String): Boolean

    fun getCurrentDownload(): Flowable<OperationStatus>?
    fun removeCurrentDownload()
}