# MD to PDF Android App

An Android application that converts Markdown files to PDF format using modern Android development practices.

## Features

- **File Selection**: Browse and select Markdown files from device storage
- **File Validation**: Validates file size (max 10MB) and readability before conversion
- **Markdown Parsing**: Converts Markdown syntax to formatted HTML using CommonMark library
- **GFM Tables Support**: Full support for GitHub Flavored Markdown tables
- **PDF Generation**: Creates professional-looking PDF documents using iText7
- **Enhanced Styling**: Improved CSS styling with better table formatting, blockquotes, and typography
- **Material Design**: Clean, modern UI following Material Design guidelines
- **Comprehensive Error Handling**: Detailed error messages for different failure scenarios
- **Storage Validation**: Checks storage availability and space before conversion
- **Permission Management**: Handles storage permissions gracefully
- **Modular Architecture**: Clean separation of concerns with utility classes

## Screenshots

The app features a simple and intuitive interface:
- Main screen with file selection button
- Convert button that becomes enabled after file selection
- Progress indicator during conversion
- Success/error feedback messages with detailed information

## Technical Details

### Architecture
- **Language**: Kotlin
- **UI Framework**: Android Views with View Binding
- **Async Operations**: Kotlin Coroutines for background processing
- **File Access**: Android Storage Access Framework (SAF)
- **Design Pattern**: Modular architecture with utility classes

### Dependencies
- **AndroidX Core**: Core Android components
- **Material Components**: Material Design UI components
- **CommonMark**: Markdown parsing library (org.commonmark:commonmark:0.21.0)
- **GFM Tables Extension**: GitHub Flavored Markdown tables (org.commonmark:commonmark-ext-gfm-tables:0.21.0)
- **iText7**: PDF generation library (com.itextpdf:itext7-core:7.2.5)

### Key Components

1. **MainActivity.kt**: Main application logic
   - File picker implementation using ActivityResultContracts
   - Permission handling for storage access
   - UI state management
   - Coordinating conversion workflow

2. **Utility Classes** (in `com.md2pdf.android.util` package):
   - **MarkdownConverter**: Handles Markdown to HTML conversion with GFM tables support
   - **PdfGenerator**: Manages PDF file generation from HTML
   - **FileValidator**: Validates files before conversion (size, readability)
   - **FileUtils**: File system operations and storage checks

3. **Layout**: Clean Material Design interface
   - File selection button
   - Conversion button with enabled/disabled states
   - Progress indicator
   - Status messages with detailed feedback

## Code Enhancements

### What's New

1. **Modular Architecture**: Code refactored into separate utility classes for better maintainability
2. **File Validation**: Files are validated before conversion to prevent errors
3. **Enhanced Error Handling**: Specific error messages for different failure scenarios
4. **Storage Checks**: Validates storage availability and space before conversion
5. **Improved Styling**: Better CSS for tables, blockquotes, and overall typography
6. **GFM Tables**: Full support for GitHub Flavored Markdown tables with enhanced styling
7. **Better Feedback**: Users receive detailed information about conversion success or failure

## Build Instructions

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK API 24+ (Android 7.0+)
- Java 8 or later

### Building the APK

1. Clone this repository:
   ```bash
   git clone https://github.com/yji0728/md2pdf_android.git
   cd md2pdf_android
   ```

2. Open the project in Android Studio

3. Sync the project with Gradle files

4. Build the APK:
   ```bash
   ./gradlew assembleDebug
   ```

5. The APK will be generated in `app/build/outputs/apk/debug/`

### Installing on Device

1. Enable "Unknown Sources" in your Android device settings
2. Transfer the APK to your device
3. Install the APK by tapping on it

## Usage

1. **Launch the app** on your Android device
2. **Grant permissions** when prompted (storage access required)
3. **Select a Markdown file** by tapping "Select Markdown File"
4. **Browse and choose** your .md file from device storage
5. **Convert to PDF** by tapping the "Convert to PDF" button
6. **Wait for conversion** - progress will be shown
7. **Find your PDF** in the Downloads folder with timestamp

## Supported Markdown Features

The app supports standard CommonMark syntax including:
- Headers (H1-H6)
- Bold and italic text
- Lists (ordered and unordered)
- Links
- Code blocks and inline code
- Blockquotes (with enhanced styling)
- Tables (with GFM extension and enhanced styling)
- Horizontal rules
- Strikethrough (when supported by parser)

## File Output

Generated PDF files are saved to the device's Downloads folder with the naming pattern:
`{original_filename}_{timestamp}.pdf`

Example: `README_20240928_143022.pdf`

## Permissions

The app requires the following permissions:
- `READ_EXTERNAL_STORAGE`: To read Markdown files
- `WRITE_EXTERNAL_STORAGE`: To save generated PDF files
- `MANAGE_EXTERNAL_STORAGE`: For broader file access (Android 11+)

## Troubleshooting

### Common Issues:

1. **Permission Denied**: Ensure storage permissions are granted in app settings
2. **File Not Found**: Verify the selected file is accessible and not corrupted
3. **Conversion Failed**: Check if the Markdown file contains valid syntax
4. **PDF Not Generated**: Ensure sufficient storage space is available

### Error Messages:
- "Please select a markdown file first" - No file selected
- "Failed to convert file" - Conversion error occurred
- "Storage permission is required" - Permission not granted

## Development Notes

### Code Structure
- Follows MVVM-like pattern with clear separation of concerns
- Uses modern Android APIs (ActivityResultContracts, View Binding)
- Implements proper error handling and user feedback
- Includes comprehensive permission management

### Future Enhancements
- Custom PDF styling options (user-selectable themes)
- Batch conversion support
- Cloud storage integration
- Markdown preview before conversion
- Custom output directory selection
- Image embedding support
- Syntax highlighting for code blocks
- Export to different page sizes

## License

This project is open source and available under the MIT License.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

---

**Note**: This app is designed for Android 7.0 (API 24) and above. For optimal experience, use Android 8.0 or later.