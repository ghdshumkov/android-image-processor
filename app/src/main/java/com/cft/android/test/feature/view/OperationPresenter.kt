package com.cft.android.test.feature.view

import android.net.Uri
import android.util.Log
import com.cft.android.test.feature.model.IOperationManager
import com.cft.android.test.feature.model.StatusProgress
import com.cft.android.test.feature.model.StatusResult
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy

/**
 * Created by administrator <dshumkov@icerockdev.com> on 20.10.18.
 */
class OperationPresenter(private val operationManager: IOperationManager): Presenter<IOperationView>(), IOperationListener {

    var selectedImageUri: Uri? = null
    set(value) {
        field = value
        value?.let {
            view?.showSelectedImage(it)
        }
    }

    private var mSelectedItemPos: Int? = null

    private var mDownloadDisposable: Disposable? = null

    override fun onAttachViewInterface(view: IOperationView) {
        super.onAttachViewInterface(view)

        view.renderList(operationManager.operations)
        subscribeDownload()

        this.selectedImageUri = selectedImageUri
    }

    override fun onDetachViewInterface() {
        super.onDetachViewInterface()
        mDownloadDisposable?.dispose()
    }

    override fun onRotateClick() {
        selectedImageUri?.let {
            val newIndex = operationManager.newRotateOperation(it)
            view?.updateListForNewOperation(newIndex)
        }
    }

    override fun onGrayscaleClick() {
        selectedImageUri?.let {
            val newIndex = operationManager.newGrayscaleOperation(it)
            view?.updateListForNewOperation(newIndex)
        }
    }

    override fun onFlipClick() {
        selectedImageUri?.let {
            val newIndex = operationManager.newFlipOperation(it)
            view?.updateListForNewOperation(newIndex)
        }
    }

    fun download(url: String) {
        if (operationManager.newDownloadOperation(url.trim())) {
            subscribeDownload()
        }
        else {
            //TODO: not support multiple download operations
            // Wait until current finished
        }
    }

    fun checkItemResult(selectedPosition: Int): Boolean {
        val status = operationManager.operations[selectedPosition].getStatus()
        return when (status) {
            is StatusResult -> {
                mSelectedItemPos = selectedPosition
                true
            }
            else -> false
        }
    }

    fun useUriOfSelectedItem() {
        mSelectedItemPos?.let { pos ->
            val status = operationManager.operations[pos].getStatus()
            when (status) {
                is StatusResult -> {
                    this.selectedImageUri = status.uri
                }
                else -> { }
            }
        }
    }

    fun removeSelectedItem() {
        mSelectedItemPos?.let {
            operationManager.operations.removeAt(it)
            view?.updateListForRemoveOperation(it)
        }
    }

    private fun subscribeDownload() {
        mDownloadDisposable = operationManager.getCurrentDownload()?.let {
            it.observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe {
                        view?.showDownloadProgress(0)
                    }
                    .subscribeBy(
                            onNext = {
                                when (it) {
                                    is StatusProgress -> {
                                        Log.d("Download progress: %s", it.progress.toString())
                                        view?.showDownloadProgress(it.progress)
                                    }
                                    is StatusResult -> {
                                        this.selectedImageUri = it.uri
                                        Log.d("Download result: %s", it.uri.toString())
                                    }
                                }
                            },
                            onError = {
                                operationManager.removeCurrentDownload()
                                view?.dismissDownloadProgress()
                                view?.showErrorMessage(it.localizedMessage)
                            },
                            onComplete = {
                                operationManager.removeCurrentDownload()
                                view?.dismissDownloadProgress()
                            }
                    )
        }
    }
}