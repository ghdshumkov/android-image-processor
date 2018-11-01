package com.cft.android.test.feature.view

import android.net.Uri
import com.cft.android.test.feature.model.OperationStatus
import com.cft.android.test.feature.model.operation.OperationStatusObserver


/**
 * Created by administrator <dshumkov@icerockdev.com> on 20.10.18.
 */
interface IOperationView {

    // For image operation list
    fun renderList(list: List<OperationStatusObserver>)
    fun updateListForNewOperation(newPosition: Int)
    fun updateListForRemoveOperation(removePos: Int)

    fun showSelectedImage(imageUri: Uri)

    // For download image operation
    fun showDownloadProgress(progress: Int)
    fun dismissDownloadProgress()

    // Error message
    fun showErrorMessage(errorText: String)
}