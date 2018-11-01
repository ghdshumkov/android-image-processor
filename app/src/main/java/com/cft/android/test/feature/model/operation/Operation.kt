package com.cft.android.test.feature.model.operation

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.cft.android.test.feature.model.OperationStatus
import com.cft.android.test.feature.model.StatusProgress
import com.cft.android.test.feature.model.StatusResult
import com.cft.android.test.feature.util.MediaUtils
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.ReplaySubject
import java.util.concurrent.TimeUnit
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


/**
 * Created by administrator <dshumkov@icerockdev.com> on 24.10.18.
 */
abstract class Operation(private val context: Context, private val inputUri: Uri) : IOperation, OperationStatusObserver {

    // buffered only one recent result (progress or result)
    private val mHotStatus = ReplaySubject.createWithSize<OperationStatus>(1)

    override fun operationStatus(): Observable<OperationStatus> {
        return mHotStatus
    }

    fun getStatus(): OperationStatus? {
        return mHotStatus.value
    }

    override fun execute() {

        // Generate random time seconds from (5..30) sec
        val random = Random()
        val secs = random.nextInt(25) + 5
        val step = secs * 10L

        Observable.combineLatest(Observable.interval(step, TimeUnit.MILLISECONDS).take(100)
                .doOnNext { mHotStatus.onNext(StatusProgress(it.toInt())) },
                Single.fromCallable {

                    Log.d("execute with uri: %s", inputUri.toString())

                    val bitmap = MediaUtils.getScaleBitmap(context, inputUri, 1024, 1024)
                    val result = applyOperation(bitmap)
                    writeBitmapToFile(result)
                }.toObservable(),
                BiFunction<Long, Uri, Pair<Long, Uri>> { progress, result -> progress to result })
                .filter {
                    it.first == 99L
                }
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                        onNext = {
                            Log.d("Finish result: %s", it.second.toString())
                            // result status
                            mHotStatus.onNext(StatusResult(it.second))
                        },
                        onError = {

                            // TODO: handle error for Status operation
                            mHotStatus.onError(it)
                            Log.e("Error", "Error", it)
                        },
                        onComplete = {
                            mHotStatus.onComplete()
                            Log.d("Finish complete: %s", "Oncomplete")
                        }
                )
    }

    protected abstract fun applyOperation(inputBitmap: Bitmap): Bitmap

    private fun writeBitmapToFile(outputBitmap: Bitmap): Uri {

        val name = String.format("filter-output-%s.png", UUID.randomUUID().toString())
        val outputDir = File(context.filesDir, "operations")
        if (!outputDir.exists()) {
            outputDir.mkdirs() // should succeed
        }
        val outputFile = File(outputDir, name)
        var out: FileOutputStream? = null
        try {
            Log.d("writeToFile with: %s", outputFile.absolutePath)
            out = FileOutputStream(outputFile)
            outputBitmap.compress(Bitmap.CompressFormat.PNG, 0 /* ignored for PNG */, out)
        } finally {
            if (out != null) {
                try {
                    out.close()
                } catch (ignore: IOException) {
                }

            }
        }
        return Uri.fromFile(outputFile)
    }
}

interface OperationStatusObserver {
    fun operationStatus(): Observable<OperationStatus>
}