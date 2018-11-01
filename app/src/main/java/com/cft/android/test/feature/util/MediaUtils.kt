package com.cft.android.test.feature.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.support.v4.content.FileProvider
import android.util.Log
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by administrator <dshumkov@icerockdev.com> on 21.10.18.
 */
object MediaUtils {

    fun createTempImageFile(ctx: Context): File? {
        val timeStamp = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault()).format(Date())
        val storageDir = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        try {
            return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        }
        catch (io : IOException) {
            Log.e("MediaUtils", "Unable to create temp file for image", io)
        }
        return null
    }

    fun getUriForFile(ctx: Context, file: File): Uri {
        return FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
    }


    fun getScaleBitmap(ctx: Context, uri: Uri, maxHeight: Int, maxWidth: Int): Bitmap {

        var inputStream: InputStream? = null
        try {

            inputStream = ctx.contentResolver.openInputStream(uri)

            val bmOptions = BitmapFactory.Options().apply {
                // Get the dimensions of the original bitmap
                inJustDecodeBounds = true
            }

            BitmapFactory.decodeStream(inputStream, null, bmOptions)

            bmOptions.apply {

                // Determine how much to scale down the output image
                val scaleFactor = Math.min(outWidth / maxWidth, outHeight / maxHeight)

                inJustDecodeBounds = false
                inSampleSize = scaleFactor
                inPurgeable = true
            }

            inputStream = ctx.contentResolver.openInputStream(uri)
            return BitmapFactory.decodeStream(inputStream, null, bmOptions)
        }
        finally {
            if (inputStream != null) {
                inputStream.close()
            }
        }
    }
}