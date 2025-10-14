package com.md2pdf.android.util

import com.itextpdf.html2pdf.HtmlConverter
import java.io.File
import java.io.FileOutputStream

/**
 * Utility class for generating PDF files from HTML content
 */
object PdfGenerator {
    
    /**
     * Converts HTML content to PDF and saves it to a file
     * @param htmlContent The HTML content to convert
     * @param outputFile The file where PDF will be saved
     * @return true if conversion was successful, false otherwise
     */
    fun generatePdf(htmlContent: String, outputFile: File): Boolean {
        return try {
            if (htmlContent.isBlank()) {
                throw IllegalArgumentException("HTML content cannot be empty")
            }
            
            // Ensure parent directory exists
            outputFile.parentFile?.mkdirs()
            
            // Convert HTML to PDF
            FileOutputStream(outputFile).use { outputStream ->
                HtmlConverter.convertToPdf(htmlContent, outputStream)
            }
            
            // Verify file was created and has content
            outputFile.exists() && outputFile.length() > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
