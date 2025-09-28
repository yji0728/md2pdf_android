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
            
            // Parse markdown to HTML
            val parser = Parser.builder().build()
            val document = parser.parse(markdownContent)
            val htmlRenderer = HtmlRenderer.builder().build()
            val htmlContent = htmlRenderer.render(document)
            
            // Create full HTML document
            val fullHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; margin: 40px; line-height: 1.6; }
                        h1, h2, h3, h4, h5, h6 { color: #333; }
                        code { background-color: #f4f4f4; padding: 2px 4px; border-radius: 3px; }
                        pre { background-color: #f4f4f4; padding: 10px; border-radius: 5px; overflow-x: auto; }
                        blockquote { border-left: 4px solid #ddd; margin: 0; padding-left: 20px; color: #666; }
                        table { border-collapse: collapse; width: 100%; }
                        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                        th { background-color: #f2f2f2; }
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