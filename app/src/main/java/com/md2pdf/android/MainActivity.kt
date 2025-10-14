package com.md2pdf.android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.md2pdf.android.databinding.ActivityMainBinding
import com.md2pdf.android.util.FileUtils
import com.md2pdf.android.util.FileValidator
import com.md2pdf.android.util.MarkdownConverter
import com.md2pdf.android.util.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                handleFileSelection(uri)
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
    
    private fun handleFileSelection(uri: Uri) {
        // Validate file before accepting it
        when (val result = FileValidator.validateFile(this, uri)) {
            is FileValidator.ValidationResult.Valid -> {
                selectedFileUri = uri
                fileName = FileValidator.getFileName(this, uri)
                binding.selectedFileText.text = getString(R.string.file_selected, fileName)
                binding.selectedFileText.visibility = View.VISIBLE
                binding.convertButton.isEnabled = true
                binding.statusText.text = ""
            }
            is FileValidator.ValidationResult.Invalid -> {
                Toast.makeText(this, "Invalid file: ${result.reason}", Toast.LENGTH_LONG).show()
                binding.statusText.text = "Error: ${result.reason}"
            }
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

    private fun convertToPdf() {
        selectedFileUri?.let { uri ->
            // Check storage availability
            if (!FileUtils.isExternalStorageWritable()) {
                Toast.makeText(this, getString(R.string.storage_not_available), Toast.LENGTH_LONG).show()
                binding.statusText.text = getString(R.string.storage_not_available)
                return
            }
            
            // Check available space
            if (!FileUtils.hasEnoughSpace()) {
                Toast.makeText(this, getString(R.string.insufficient_storage), Toast.LENGTH_LONG).show()
                binding.statusText.text = getString(R.string.insufficient_storage)
                return
            }
            
            binding.progressBar.visibility = View.VISIBLE
            binding.convertButton.isEnabled = false
            binding.statusText.text = getString(R.string.converting)
            
            lifecycleScope.launch {
                try {
                    val result = withContext(Dispatchers.IO) {
                        performConversion(uri)
                    }
                    
                    when (result) {
                        is ConversionResult.Success -> {
                            binding.statusText.text = getString(R.string.conversion_successful_with_path, result.outputPath)
                            Toast.makeText(this@MainActivity, getString(R.string.conversion_successful), Toast.LENGTH_LONG).show()
                        }
                        is ConversionResult.Failure -> {
                            binding.statusText.text = getString(R.string.conversion_failed_reason, result.reason)
                            Toast.makeText(this@MainActivity, result.reason, Toast.LENGTH_LONG).show()
                        }
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
    
    private sealed class ConversionResult {
        data class Success(val outputPath: String) : ConversionResult()
        data class Failure(val reason: String) : ConversionResult()
    }

    private suspend fun performConversion(uri: Uri): ConversionResult {
        return try {
            // Read markdown content
            val markdownContent = FileValidator.readFileContent(this, uri)
                ?: return ConversionResult.Failure("Cannot read file content")
            
            if (markdownContent.isBlank()) {
                return ConversionResult.Failure("File is empty")
            }
            
            // Convert markdown to HTML
            val htmlContent = try {
                MarkdownConverter.convertToHtml(markdownContent, fileName)
            } catch (e: Exception) {
                return ConversionResult.Failure("Markdown parsing failed: ${e.message}")
            }
            
            // Generate PDF
            val outputFile = FileUtils.createOutputFile(fileName)
            val success = PdfGenerator.generatePdf(htmlContent, outputFile)
            
            if (success) {
                ConversionResult.Success(outputFile.absolutePath)
            } else {
                ConversionResult.Failure("PDF generation failed")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ConversionResult.Failure("Unexpected error: ${e.message}")
        }
    }
}