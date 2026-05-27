package com.example.lbo_marketplace.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

suspend fun compressImage(
    context: Context,
    uri: Uri
): File {

    return withContext(Dispatchers.IO) {

        // =====================================================
        // 🔥 TEMP FILE
        // =====================================================

        val tempFile = File.createTempFile(
            "compressed_",
            ".jpg",
            context.cacheDir
        )

        // =====================================================
        // 🔥 COPY URI TO TEMP FILE SAFELY
        // =====================================================

        context.contentResolver
            .openInputStream(uri)
            ?.use { inputStream ->

                tempFile.outputStream()
                    .use { outputStream ->

                        inputStream.copyTo(
                            outputStream
                        )
                    }
            }

        // =====================================================
        // 🔥 COMPRESS IMAGE
        // =====================================================

        Compressor.compress(
            context,
            tempFile
        ) {

            // 🔥 REDUCED RESOLUTION
            resolution(720, 720)

            // 🔥 STRONG COMPRESSION
            quality(60)

            // 🔥 JPEG FORMAT
            format(Bitmap.CompressFormat.JPEG)
        }
    }
}