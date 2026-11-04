package com.example.sa_event_hub

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.ByteArrayOutputStream

// Makes a big phone photo small before it is uploaded (saves data and time).
object ImageUtils {

    // Reads the picture, shrinks it so the longest side is at most maxSize pixels,
    // and returns it as JPEG bytes. Returns null if the picture cannot be read.
    fun toJpegBytes(context: Context, uri: Uri, maxSize: Int = 512): ByteArray? {
        return try {
            // 1. Read only the size, so we do not load a huge picture into memory
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            }

            var sample = 1
            while (bounds.outWidth / sample > maxSize * 2 || bounds.outHeight / sample > maxSize * 2) {
                sample *= 2
            }

            // 2. Load a smaller copy
            val options = BitmapFactory.Options().apply { inSampleSize = sample }
            val bitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: return null

            // 3. Scale to the final size
            val scale = maxSize.toFloat() / maxOf(bitmap.width, bitmap.height)
            val scaled = if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true
                )
            } else bitmap

            // 4. Save as JPEG
            val out = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
            Log.d("ImageUtils", "Picture ready: ${scaled.width}x${scaled.height}, ${out.size()} bytes")
            out.toByteArray()
        } catch (e: Exception) {
            Log.e("ImageUtils", "Could not read the picture", e)
            null
        }
    }
}
