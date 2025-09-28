package com.md2pdf.android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.md2pdf.android.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import com.itextpdf.html2pdf.HtmlConverter
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var selectedFileUri: Uri? = null
    private var fileName: String? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            openFilePicker()
        } else {
            Toast.makeText(this, getString(R.string.permission_required), Toast.LENGTH_LONG).show()
        }
    }

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedFileUri = uri
                fileName = getFileName(uri)
                binding.selectedFileText.text = getString(R.string.file_selected, fileName)
                binding.selectedFileText.visibility = View.VISIBLE
                binding.convertButton.isEnabled = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.selectFileButton.setOnClickListener {
            checkPermissionsAndOpenFilePicker()
        }

        binding.convertButton.setOnClickListener {
            convertToPdf()
        }
    }

    private fun checkPermissionsAndOpenFilePicker() {
        val permissions = mutableListOf<String>()
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissions.isNotEmpty()) {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        } else {
            openFilePicker()
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/markdown", "text/plain", "text/*"))
        }
        filePickerLauncher.launch(intent)
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
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

    private fun convertToPdf() {
        selectedFileUri?.let { uri ->
            binding.progressBar.visibility = View.VISIBLE
            binding.convertButton.isEnabled = false
            binding.statusText.text = getString(R.string.converting)
            
            lifecycleScope.launch {
                try {
                    val success = withContext(Dispatchers.IO) {
                        performConversion(uri)
                    }
                    
                    if (success) {
                        binding.statusText.text = getString(R.string.conversion_successful)
                        Toast.makeText(this@MainActivity, getString(R.string.conversion_successful), Toast.LENGTH_LONG).show()
                    } else {
                        binding.statusText.text = getString(R.string.conversion_failed)
                        Toast.makeText(this@MainActivity, getString(R.string.conversion_failed), Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    binding.statusText.text = getString(R.string.conversion_failed)
                    Toast.makeText(this@MainActivity, "${getString(R.string.conversion_failed)}: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    binding.progressBar.visibility = View.GONE
                    binding.convertButton.isEnabled = true
                }
            }
        }
    }

    private suspend fun performConversion(uri: Uri): Boolean {
        return try {
            // Read markdown content
            val markdownContent = readFileContent(uri)
            
            if (markdownContent.isBlank()) {
                return false
            }
            
            // Parse markdown to HTML
            val parser = Parser.builder().build()
            val document = parser.parse(markdownContent)
            val htmlRenderer = HtmlRenderer.builder().build()
            val htmlContent = htmlRenderer.render(document)
            
            // Create full HTML document with enhanced styling
            val fullHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>$fileName</title>
                    <style>
                        body { 
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
                            margin: 40px; 
                            line-height: 1.6; 
                            color: #333;
                            font-size: 14px;
                        }
                        h1, h2, h3, h4, h5, h6 { 
                            color: #2c3e50; 
                            margin-top: 24px;
                            margin-bottom: 16px;
                        }
                        h1 { font-size: 28px; border-bottom: 2px solid #3498db; padding-bottom: 8px; }
                        h2 { font-size: 24px; border-bottom: 1px solid #bdc3c7; padding-bottom: 4px; }
                        h3 { font-size: 20px; }
                        h4 { font-size: 18px; }
                        h5 { font-size: 16px; }
                        h6 { font-size: 14px; }
                        p { margin-bottom: 16px; }
                        code { 
                            background-color: #f8f9fa; 
                            padding: 2px 6px; 
                            border-radius: 3px; 
                            font-family: 'Consolas', 'Monaco', monospace;
                            font-size: 13px;
                            color: #e74c3c;
                        }
                        pre { 
                            background-color: #f8f9fa; 
                            padding: 16px; 
                            border-radius: 6px; 
                            overflow-x: auto;
                            border: 1px solid #e1e8ed;
                            margin: 16px 0;
                        }
                        pre code {
                            background-color: transparent;
                            padding: 0;
                            color: #333;
                        }
                        blockquote { 
                            border-left: 4px solid #3498db; 
                            margin: 16px 0; 
                            padding-left: 20px; 
                            color: #7f8c8d;
                            font-style: italic;
                        }
                        table { 
                            border-collapse: collapse; 
                            width: 100%; 
                            margin: 16px 0;
                            font-size: 13px;
                        }
                        th, td { 
                            border: 1px solid #bdc3c7; 
                            padding: 10px 12px; 
                            text-align: left; 
                        }
                        th { 
                            background-color: #ecf0f1; 
                            font-weight: 600;
                            color: #2c3e50;
                        }
                        tr:nth-child(even) {
                            background-color: #f8f9fa;
                        }
                        ul, ol {
                            padding-left: 24px;
                            margin: 16px 0;
                        }
                        li {
                            margin: 4px 0;
                        }
                        a {
                            color: #3498db;
                            text-decoration: none;
                        }
                        a:hover {
                            text-decoration: underline;
                        }
                        strong {
                            color: #2c3e50;
                        }
                        em {
                            color: #7f8c8d;
                        }
                    </style>
                </head>
                <body>
                $htmlContent
                </body>
                </html>
            """.trimIndent()
            
            // Generate PDF
            val outputFile = createOutputFile()
            val outputStream = FileOutputStream(outputFile)
            
            HtmlConverter.convertToPdf(fullHtml, outputStream)
            outputStream.close()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun readFileContent(uri: Uri): String {
        val inputStream = contentResolver.openInputStream(uri)
        return inputStream?.use { stream ->
            stream.bufferedReader().use { reader ->
                reader.readText()
            }
        } ?: ""
    }

    private fun createOutputFile(): File {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val baseFileName = fileName?.substringBeforeLast('.') ?: "markdown"
        val outputFileName = "${baseFileName}_$timestamp.pdf"
        return File(downloadsDir, outputFileName)
    }
}