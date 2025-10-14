# Changelog

All notable changes to the MD to PDF Android App will be documented in this file.

## [Enhanced] - 2024

### Added
- **Modular Architecture**: Refactored codebase into separate utility classes
  - `MarkdownConverter`: Handles all Markdown to HTML conversion logic
  - `PdfGenerator`: Manages PDF file generation
  - `FileValidator`: Validates files before conversion
  - `FileUtils`: Handles file system operations
- **File Validation**: Pre-conversion validation of files
  - Maximum file size limit (10MB)
  - Readability checks
  - Empty file detection
  - Detailed error messages for validation failures
- **Storage Checks**: Pre-conversion storage validation
  - External storage availability check
  - Free space verification (minimum 10MB)
  - User-friendly error messages
- **Enhanced Error Handling**:
  - Sealed class pattern for conversion results
  - Specific error messages for different failure types
  - Better exception management with detailed feedback
- **Improved PDF Styling**:
  - Enhanced table styling with blue headers and hover effects
  - Better blockquote rendering with background and rounded corners
  - Improved code block styling
  - Better typography and spacing
- **GFM Tables Support**: Full integration of GitHub Flavored Markdown tables extension
- **Detailed User Feedback**:
  - Success messages include output file path
  - Failure messages include specific reasons
  - Better status updates during conversion

### Changed
- **MainActivity.kt**: Simplified and focused on UI logic and orchestration
  - Removed inline conversion logic
  - Removed inline HTML/CSS generation
  - Removed file utility methods
  - Added validation before conversion
  - Improved error handling with sealed classes
- **String Resources**: Added new strings for enhanced error messages
  - `conversion_successful_with_path`: Shows output location
  - `conversion_failed_reason`: Shows specific failure reason
  - `storage_not_available`: Storage unavailable error
  - `insufficient_storage`: Insufficient space error
- **CSS Styling**: Enhanced visual appearance of generated PDFs
  - Table headers now use blue background with white text
  - Alternating row colors for better readability
  - Hover effects on table rows
  - Blockquotes have background color and rounded corners
  - Better font weights and colors throughout

### Improved
- **Code Maintainability**: Clear separation of concerns
- **Testability**: Utility classes can be easily unit tested
- **Error Messages**: More specific and actionable feedback
- **User Experience**: Better validation and feedback
- **PDF Quality**: Enhanced styling and formatting
- **Code Reusability**: Utility classes can be used independently

### Technical Details
- **Package Structure**: Added `com.md2pdf.android.util` package
- **Kotlin Features**: Leveraged sealed classes for type-safe results
- **Error Handling**: Multi-layered error handling with specific messages
- **Resource Management**: Proper use of `use {}` for automatic resource cleanup

## [1.0] - Initial Release

### Features
- Markdown to PDF conversion
- File picker integration
- Basic error handling
- Material Design UI
- Storage permission management
- CommonMark markdown parser
- iText7 PDF generation
- Basic CSS styling
