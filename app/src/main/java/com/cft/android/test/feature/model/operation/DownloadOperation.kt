package com.cft.android.test.feature.model.operation

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.cft.android.test.core.dagger.DaggerAppComponent
import com.cft.android.test.feature.model.OperationStatus
import com.cft.android.test.feature.model.StatusProgress
import com.cft.android.test.feature.model.StatusResult
import com.cft.android.test.feature.util.MediaUtils
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.ReplaySubject
import io.reactivex.subjects.Subject
import okhttp3.*
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.Okio
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import okio.BufferedSink


/**
 * Created by administrator <dshumkov@icerockdev.com> on 27.10.18.
 */
class DownloadOperation(private val context: Context, private val url: String) : IOperation, OperationStatusObserver {

    private val mHotStatus = ReplaySubject.createWithSize<OperationStatus>(1)

    override fun operationStatus(): Observable<OperationStatus> {
        return mHotStatus
    }

    override fun execute() {

        Single.fromCallable {
            val client = OkHttpClient.Builder().addInterceptor({
                val originalResponse = it.proceed(it.request())
                originalResponse.newBuilder()
                        .body(ProgressResponse(originalResponse.body()!!, mHotStatus))
                        .build()
            }).build()

            val request = Request.Builder()
                    .url(url)
                    .build()

            val response = client.newCall(request).execute()
            Log.d("DownloadOperation", "code: ${response.code()}")

            if (!response.isSuccessful) throw Exception("Bad response with code: ${response.code()}")

            if (!response.body()!!.contentType()!!.type().contentEquals("image")) {
                throw Exception("Bad request with unsupported type: ${response.body()!!.contentType()!!.type()}")
            }

            val outFile = MediaUtils.createTempImageFile(context) ?: throw Exception("Unable save image")
            val sink = Okio.buffer(Okio.sink(outFile))
            sink.writeAll(response.body()!!.source())
            sink.close()
            MediaUtils.getUriForFile(context, outFile)

        }
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                        onSuccess = {
                            Log.d("Finish download", it.toString())
                            mHotStatus.onNext(StatusResult(it))
                            mHotStatus.onComplete()
                        },
                        onError = {
                            // TODO: handle error for Status operation
                            Log.e("Download Error", "Error", it)
                            mHotStatus.onError(it)
                        }
                )
    }

    private class ProgressResponse(private val responseBody: ResponseBody, private val observer: Subject<OperationStatus>) : ResponseBody() {

        private var bufferedSource: BufferedSource? = null

        override fun contentType(): MediaType? {
            return responseBody.contentType()
        }

        override fun contentLength(): Long {
            return responseBody.contentLength()
        }

        override fun source(): BufferedSource {
            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(object : ForwardingSource(responseBody.source()) {

                    var totalBytesRead = 0L
                    override fun read(sink: Buffer, byteCount: Long): Long {

                        Log.d("ProgressResponse", "lenght: ${responseBody.contentLength()}, byteCount: $byteCount")

                        val bytesRead = super.read(sink, byteCount)
                        // read() returns the number of bytes read, or -1 if this source is exhausted.
                        totalBytesRead += if (bytesRead != -1L) bytesRead else 0
//                    progressListener.update(totalBytesRead, responseBody.contentLength(), bytesRead == -1)
                        observer.onNext(StatusProgress((totalBytesRead.toInt() * 100) / responseBody.contentLength().toInt()))
                        return bytesRead
                    }
                })
            }
            return bufferedSource!!
        }
    }

}