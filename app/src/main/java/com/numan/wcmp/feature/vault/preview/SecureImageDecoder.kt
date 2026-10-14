package com.numan.wcmp.feature.vault.preview

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max

interface PreviewImageData

class AndroidPreviewImageData(val bitmap: Bitmap) : PreviewImageData

class FakePreviewImageData : PreviewImageData

interface SecureImageDecoder {
    suspend fun decode(
        file: File,
        targetWidth: Int,
        targetHeight: Int
    ): PreviewImageData
}

class DefaultSecureImageDecoder : SecureImageDecoder {
    override suspend fun decode(file: File, targetWidth: Int, targetHeight: Int): PreviewImageData {
        return withContext(Dispatchers.IO) {
            if (!file.exists() || targetWidth <= 0 || targetHeight <= 0) {
                throw IllegalArgumentException("Invalid file or dimensions")
            }

            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(file.absolutePath, options)

            val sourceWidth = options.outWidth
            val sourceHeight = options.outHeight

            if (sourceWidth <= 0 || sourceHeight <= 0) {
                throw IllegalArgumentException("Failed to decode image bounds")
            }

            options.inSampleSize = calculateInSampleSize(sourceWidth, sourceHeight, targetWidth, targetHeight)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888

            val bitmap = BitmapFactory.decodeFile(file.absolutePath, options) 
                ?: throw IllegalStateException("Failed to decode bitmap")
            
            AndroidPreviewImageData(bitmap)
        }
    }

    private fun calculateInSampleSize(
        sourceWidth: Int,
        sourceHeight: Int,
        targetWidth: Int,
        targetHeight: Int
    ): Int {
        var inSampleSize = 1
        if (sourceHeight > targetHeight || sourceWidth > targetWidth) {
            val halfHeight = sourceHeight / 2
            val halfWidth = sourceWidth / 2
            while (halfHeight / inSampleSize >= targetHeight && halfWidth / inSampleSize >= targetWidth) {
                inSampleSize *= 2
            }
        }
        return max(1, inSampleSize)
    }
}
