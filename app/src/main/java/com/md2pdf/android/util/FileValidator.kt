package com.md2pdf.android.util

import android.content.Context
import android.net.Uri
import java.io.IOException

/**
 * Utility class for validating files before conversion
 */
object FileValidator {
    
    // Maximum file size: 10 MB
    private const val MAX_FILE_SIZE = 10 * 1024 * 1024L
    
    /**
     * Result of file validation
     */
    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()
    }
    
    /**
     * Validates a file for markdown to PDF conversion
     * @param context Android context for accessing ContentResolver
     * @param uri The URI of the file to validate
     * @return ValidationResult indicating if file is valid or why it's invalid
     */
    fun validateFile(context: Context, uri: Uri): ValidationResult {
        return try {
            // Check if file can be opened
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return ValidationResult.Invalid("Cannot open file")
            
            inputStream.use { stream ->
                // Check file size
                val fileSize = stream.available().toLong()
                if (fileSize == 0L) {
                    return ValidationResult.Invalid("File is empty")
                }
                
                if (fileSize > MAX_FILE_SIZE) {
                    val sizeMB = fileSize / (1024 * 1024)
                    return ValidationResult.Invalid("File is too large (${sizeMB}MB). Maximum size is 10MB")
                }
                
                // Try to read first few bytes to ensure file is readable
                val buffer = ByteArray(1024)
                val bytesRead = stream.read(buffer)
                if (bytesRead <= 0) {
                    return ValidationResult.Invalid("Cannot read file content")
                }
            }
            
            ValidationResult.Valid
        } catch (e: IOException) {
            ValidationResult.Invalid("IO error: ${e.message}")
        } catch (e: Exception) {
            ValidationResult.Invalid("Unexpected error: ${e.message}")
        }
    }
    
    /**
     * Reads file content safely with validation
     * @param context Android context for accessing ContentResolver
     * @param uri The URI of the file to read
     * @return File content as String, or null if read fails
     */
    fun readFileContent(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader().use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Gets the display name of a file from its URI
     * @param context Android context for accessing ContentResolver
     * @param uri The URI of the file
     * @return Display name of the file
     */
    fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayNameIndex = it.getColumnIndex("_display_name")
                    if (displayNameIndex >= 0) {
                        result = it.getString(displayNameIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "unknown_file"
    }
}
