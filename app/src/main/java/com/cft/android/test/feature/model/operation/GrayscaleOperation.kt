package com.cft.android.test.feature.model.operation

import android.content.Context
import android.net.Uri
import android.graphics.*




/**
 * Created by administrator <dshumkov@icerockdev.com> on 27.10.18.
 */
class GrayscaleOperation(context: Context, uri: Uri) : Operation(context, uri) {

    override fun applyOperation(inputBitmap: Bitmap): Bitmap {
        val width = inputBitmap.width
        val height = inputBitmap.height
        val outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(outBitmap)
        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(inputBitmap, 0F, 0F, paint)
        return outBitmap
    }
}