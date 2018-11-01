package com.cft.android.test.feature.model.operation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri


/**
 * Created by administrator <dshumkov@icerockdev.com> on 27.10.18.
 */
class FlipOperation(context: Context, uri: Uri) : Operation(context, uri)  {

    override fun applyOperation(inputBitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.preScale(-1.0f, 1.0f)
        return Bitmap.createBitmap(inputBitmap, 0, 0, inputBitmap.width, inputBitmap.height, matrix, true)
    }
}