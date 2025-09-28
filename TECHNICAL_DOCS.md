# Technical Documentation - MD to PDF Android App

## Project Structure

```
md2pdf_android/
├── app/
│   ├── build.gradle                 # App module build configuration
│   ├── proguard-rules.pro          # ProGuard configuration
│   └── src/main/
│       ├── AndroidManifest.xml     # App manifest with permissions
│       ├── java/com/md2pdf/android/
│       │   └── MainActivity.kt     # Main application logic
│       └── res/
│           ├── drawable/           # Vector drawables and icons
│           ├── layout/
│           │   └── activity_main.xml # Main UI layout
│           ├── mipmap-*/           # App launcher icons
│           ├── values/
│           │   ├── colors.xml      # Color definitions
│           │   ├── strings.xml     # String resources
│           │   └── themes.xml      # Material theme configuration
│           └── xml/                # Backup and data extraction rules
├── build.gradle                    # Project-level build configuration
├── gradle.properties              # Gradle properties
├── settings.gradle                 # Project settings
├── build.sh                       # Build automation script
├── sample.md                       # Sample markdown for testing
└── README.md                       # User documentation
```

## Architecture Overview

### Design Pattern
The app follows a simplified MVP (Model-View-Presenter) pattern:
- **View**: `activity_main.xml` and UI components
- **Presenter**: `MainActivity.kt` handles business logic
- **Model**: File system operations and conversion logic

### Key Components

#### 1. MainActivity.kt
**Primary Responsibilities:**
- UI state management
- Permission handling
- File selection using Storage Access Framework
- Asynchronous conversion operations
- Error handling and user feedback

**Key Methods:**
- `checkPermissionsAndOpenFilePicker()`: Handles storage permissions
- `openFilePicker()`: Launches document picker using ActivityResultContracts
- `convertToPdf()`: Orchestrates the conversion process
- `performConversion()`: Core conversion logic (Markdown → HTML → PDF)

#### 2. UI Components
**Layout Structure:**
- Title header
- File selection button
- Selected file display
- Convert button (disabled until file selected)
- Progress indicator
- Status messages

**Interactive Elements:**
- Uses Material Design components
- Proper state management (enabled/disabled buttons)
- Visual feedback during operations

## Technical Implementation

### Asynchronous Processing
```kotlin
lifecycleScope.launch {
    val success = withContext(Dispatchers.IO) {
        performConversion(uri)
    }
    // Update UI on success/failure
}
```

### File Access
Uses Android's Storage Access Framework (SAF) for secure file access:
```kotlin
private val filePickerLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
) { result ->
    // Handle file selection result
}
```

### Permission Management
Handles multiple storage permissions:
- `READ_EXTERNAL_STORAGE`
- `WRITE_EXTERNAL_STORAGE`
- `MANAGE_EXTERNAL_STORAGE` (for Android 11+)

### Conversion Pipeline

1. **File Reading**: 
   - Uses ContentResolver to read from URI
   - Handles various file sources (local, cloud, etc.)

2. **Markdown Parsing**:
   - CommonMark parser for standard Markdown
   - Extensible for additional features (tables, etc.)

3. **HTML Generation**:
   - Converts parsed Markdown to HTML
   - Adds comprehensive CSS styling

4. **PDF Generation**:
   - iText7 HTML to PDF conversion
   - Maintains formatting and styling

## Dependencies

### Core Android
```gradle
implementation 'androidx.core:core-ktx:1.9.0'
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'com.google.android.material:material:1.8.0'
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
implementation 'androidx.activity:activity-ktx:1.6.1'
```

### Markdown Processing
```gradle
implementation 'org.commonmark:commonmark:0.21.0'
implementation 'org.commonmark:commonmark-ext-gfm-tables:0.21.0'
```

### PDF Generation
```gradle
implementation 'com.itextpdf:itext7-core:7.2.5'
```

## Build Configuration

### Gradle Setup
- **Compile SDK**: 33
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 33
- **Java Version**: 1.8
- **Kotlin JVM Target**: 1.8

### Build Types
- **Debug**: Standard debug build with debugging enabled
- **Release**: Optimized build with ProGuard (currently disabled)

## Styling and Theming

### CSS Styling for PDF
The generated HTML includes comprehensive CSS:
- Professional typography (Segoe UI font family)
- Proper spacing and margins
- Syntax highlighting for code blocks
- Table styling with alternating row colors
- Header styling with underlines
- Blockquote styling with left border

### Material Design Theme
- Primary color: `#FF6200EE`
- Secondary color: `#FF03DAC5`
- Uses Material Components theme as base

## Error Handling

### Exception Management
- Try-catch blocks around all major operations
- Specific error messages for different failure types
- UI feedback for all error states

### User Feedback
- Toast messages for quick feedback
- Status text for detailed information
- Progress indicators for long operations
- Button state management

## Security Considerations

### Permissions
- Requests minimum necessary permissions
- Handles permission denial gracefully
- Uses scoped storage where possible

### File Access
- Uses SAF for secure file access
- No direct file path access
- Respects user privacy settings

## Performance Optimization

### Background Processing
- All file I/O on background threads
- UI updates on main thread only
- Proper coroutine usage

### Memory Management
- Streams for file reading
- Proper resource cleanup
- No large objects in memory

## Testing Strategy

### Manual Testing
1. Install APK on device
2. Grant necessary permissions
3. Select various Markdown files
4. Verify PDF generation
5. Check file output quality

### Test Cases
- Empty markdown file
- Large markdown file
- Various Markdown syntax elements
- Permission denial scenarios
- Network connectivity issues
- Storage space limitations

## Future Enhancements

### Potential Features
1. **Custom Styling**: User-selectable PDF themes
2. **Batch Processing**: Multiple file conversion
3. **Cloud Integration**: Google Drive, Dropbox support
4. **Preview Mode**: Show HTML before PDF generation
5. **Settings Screen**: Customizable options
6. **Image Support**: Embed images in PDF
7. **Export Options**: Different PDF page sizes

### Technical Improvements
1. **Unit Tests**: Comprehensive test coverage
2. **Integration Tests**: End-to-end testing
3. **CI/CD Pipeline**: Automated builds and releases
4. **Performance Metrics**: Conversion time tracking
5. **Error Analytics**: Crash reporting integration

## Build Instructions

### Prerequisites
- Android Studio 4.2+
- Android SDK 33+
- Java 8 or higher
- Network connection for dependencies

### Build Commands
```bash
# Generate wrapper (if needed)
gradle wrapper --gradle-version 7.6

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test
```

### Output Location
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release APK: `app/build/outputs/apk/release/app-release.apk`

## Troubleshooting

### Common Build Issues
1. **Gradle sync failed**: Check network connection and repository access
2. **SDK not found**: Verify Android SDK installation
3. **Dependency resolution**: Check version compatibility

### Runtime Issues
1. **Permission denied**: Check app permissions in device settings
2. **File not found**: Verify file accessibility and format
3. **Conversion failed**: Check Markdown syntax and file size

## License and Credits

### Open Source Libraries
- **CommonMark**: Markdown parsing (BSD 2-Clause License)
- **iText7**: PDF generation (AGPL License)
- **AndroidX**: Android support libraries (Apache 2.0)

### Development
- Language: Kotlin
- IDE: Android Studio
- Build System: Gradle
- Target Platform: Android 7.0+ (API 24+)