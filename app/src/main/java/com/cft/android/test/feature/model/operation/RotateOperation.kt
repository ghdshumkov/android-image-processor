package com.cft.android.test.feature.model.operation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import com.cft.android.test.feature.model.operation.Operation


/**
 * Created by administrator <dshumkov@icerockdev.com> on 25.10.18.
 */
class RotateOperation(context: Context, uri: Uri): Operation(context, uri) {

    override fun applyOperation(inputBitmap: Bitmap): Bitmap {

        Log.d("Operation", "applyOperation for rotate")
        val matrix = Matrix()
        matrix.postRotate(90F)
        return Bitmap.createBitmap(inputBitmap, 0, 0, inputBitmap.width, inputBitmap.height, matrix, true)
    }
}