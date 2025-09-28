#!/bin/bash

# MD to PDF Android App Build Script
# This script builds the Android APK

echo "========================================="
echo "MD to PDF Android App Build Script"
echo "========================================="

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    echo "Generating Gradle wrapper..."
    gradle wrapper --gradle-version 7.6
fi

# Make gradlew executable
chmod +x ./gradlew

echo "Building the project..."
echo "This may take several minutes on first build..."

# Clean and build the project
./gradlew clean
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo "========================================="
    echo "BUILD SUCCESSFUL!"
    echo "========================================="
    echo "APK location: app/build/outputs/apk/debug/"
    echo "APK file: app-debug.apk"
    echo ""
    echo "To install on device:"
    echo "1. Enable 'Unknown Sources' in device settings"
    echo "2. Transfer APK to device"
    echo "3. Install by tapping the APK file"
    echo "========================================="
else
    echo "========================================="
    echo "BUILD FAILED!"
    echo "========================================="
    echo "Please check the error messages above."
    echo "Common issues:"
    echo "- Network connection required for dependencies"
    echo "- Android SDK not properly configured"
    echo "- Java version compatibility"
    echo "========================================="
fi