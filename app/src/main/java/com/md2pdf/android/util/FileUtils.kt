package com.md2pdf.android.util

import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for file system operations
 */
object FileUtils {
    
    /**
     * Creates an output file for PDF in the Downloads directory
     * @param originalFileName The original markdown filename
     * @return File object for the output PDF
     */
    fun createOutputFile(originalFileName: String?): File {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        
        // Ensure directory exists
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }
        
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val baseFileName = originalFileName?.substringBeforeLast('.') ?: "markdown"
        val outputFileName = "${baseFileName}_$timestamp.pdf"
        
        return File(downloadsDir, outputFileName)
    }
    
    /**
     * Checks if external storage is available for writing
     * @return true if storage is writable, false otherwise
     */
    fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }
    
    /**
     * Gets available storage space in bytes
     * @return Available space in bytes
     */
    fun getAvailableStorageSpace(): Long {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return downloadsDir.freeSpace
    }
    
    /**
     * Checks if there is enough storage space for conversion
     * @param requiredSpace Required space in bytes (default 10MB)
     * @return true if enough space is available
     */
    fun hasEnoughSpace(requiredSpace: Long = 10 * 1024 * 1024): Boolean {
        return getAvailableStorageSpace() >= requiredSpace
    }
}
