# Architecture Diagram

## Before Enhancement

```
┌─────────────────────────────────────────────────┐
│                MainActivity.kt                   │
│              (305 lines - Monolithic)            │
│                                                  │
│  ┌──────────────────────────────────────────┐  │
│  │ UI Logic & Event Handling                │  │
│  └──────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────┐  │
│  │ Permission Management                    │  │
│  └──────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────┐  │
│  │ File Picking                             │  │
│  └──────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────┐  │
│  │ File Reading                             │  │
│  └──────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────┐  │
│  │ Markdown Parsing                         │  │
│  └──────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────┐  │
│  │ HTML Generation                          │  │
│  └──────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────┐  │
│  │ CSS Styling (inline)                     │  │
│  └──────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────┐  │
│  │ PDF Generation                           │  │
│  └──────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────┐  │
│  │ File Output Management                   │  │
│  └──────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────┐  │
│  │ Error Handling                           │  │
│  └──────────────────────────────────────────┘  │
│                                                  │
└─────────────────────────────────────────────────┘

Problems:
❌ Hard to test individual components
❌ Difficult to maintain and extend
❌ Low code reusability
❌ All concerns mixed together
❌ No validation before conversion
```

## After Enhancement

```
┌─────────────────────────────────────────────────────────────┐
│                    MainActivity.kt                          │
│                  (197 lines - Orchestrator)                 │
│                                                             │
│  ┌─────────────────┐  ┌──────────────────┐                │
│  │ UI Logic        │  │ Event Handling   │                │
│  └─────────────────┘  └──────────────────┘                │
│  ┌─────────────────┐  ┌──────────────────┐                │
│  │ Permission Mgmt │  │ State Management │                │
│  └─────────────────┘  └──────────────────┘                │
│                                                             │
│  Delegates to:                                              │
│         ↓                                                   │
└─────────┼───────────────────────────────────────────────────┘
          │
    ┌─────┴──────────────────────────────────────┐
    │                                             │
    ↓                                             ↓
┌──────────────────────┐              ┌──────────────────────┐
│  FileValidator.kt    │              │  MarkdownConverter   │
│    (110 lines)       │              │       .kt            │
│                      │              │    (173 lines)       │
│  ┌────────────────┐ │              │                      │
│  │ Size Check     │ │              │  ┌────────────────┐ │
│  │ (max 10MB)     │ │              │  │ CommonMark     │ │
│  └────────────────┘ │              │  │ Parser         │ │
│  ┌────────────────┐ │              │  └────────────────┘ │
│  │ Readability    │ │              │  ┌────────────────┐ │
│  │ Check          │ │              │  │ GFM Tables     │ │
│  └────────────────┘ │              │  │ Extension      │ │
│  ┌────────────────┐ │              │  └────────────────┘ │
│  │ Empty File     │ │              │  ┌────────────────┐ │
│  │ Detection      │ │              │  │ HTML           │ │
│  └────────────────┘ │              │  │ Generation     │ │
│  ┌────────────────┐ │              │  └────────────────┘ │
│  │ Error Messages │ │              │  ┌────────────────┐ │
│  └────────────────┘ │              │  │ CSS Styling    │ │
│                      │              │  │ (Enhanced)     │ │
└──────────────────────┘              │  └────────────────┘ │
          │                           │                      │
          │                           └──────────────────────┘
          │                                     │
          ↓                                     ↓
┌──────────────────────┐              ┌──────────────────────┐
│   FileUtils.kt       │              │  PdfGenerator.kt     │
│    (59 lines)        │              │    (39 lines)        │
│                      │              │                      │
│  ┌────────────────┐ │              │  ┌────────────────┐ │
│  │ Storage State  │ │              │  │ HTML to PDF    │ │
│  │ Check          │ │              │  │ Conversion     │ │
│  └────────────────┘ │              │  └────────────────┘ │
│  ┌────────────────┐ │              │  ┌────────────────┐ │
│  │ Space          │ │              │  │ File Output    │ │
│  │ Verification   │ │              │  └────────────────┘ │
│  └────────────────┘ │              │  ┌────────────────┐ │
│  ┌────────────────┐ │              │  │ Success        │ │
│  │ Output File    │ │              │  │ Verification   │ │
│  │ Creation       │ │              │  └────────────────┘ │
│  └────────────────┘ │              │                      │
│                      │              └──────────────────────┘
└──────────────────────┘

Benefits:
✅ Easy to test each component independently
✅ Simple to maintain and extend
✅ High code reusability
✅ Clear separation of concerns
✅ Validation before conversion prevents errors
✅ Better error messages for users
```

## Conversion Flow

```
User Action
    │
    ↓
┌──────────────────────────────┐
│ 1. Select File (MainActivity)│
└──────────────────────────────┘
    │
    ↓
┌──────────────────────────────┐
│ 2. Validate File             │
│    (FileValidator)            │
│    • Size check (<10MB)      │
│    • Readability check       │
│    • Empty file check        │
└──────────────────────────────┘
    │
    ├─→ Invalid ─→ Show Error Message ─→ End
    │
    ↓ Valid
┌──────────────────────────────┐
│ 3. Check Storage             │
│    (FileUtils)                │
│    • Is writable?            │
│    • Enough space? (>10MB)   │
└──────────────────────────────┘
    │
    ├─→ Insufficient ─→ Show Error ─→ End
    │
    ↓ OK
┌──────────────────────────────┐
│ 4. Read File Content         │
│    (FileValidator)            │
└──────────────────────────────┘
    │
    ↓
┌──────────────────────────────┐
│ 5. Convert Markdown to HTML  │
│    (MarkdownConverter)        │
│    • Parse with CommonMark   │
│    • Apply GFM tables        │
│    • Add CSS styling         │
└──────────────────────────────┘
    │
    ├─→ Parse Error ─→ Show Error ─→ End
    │
    ↓ Success
┌──────────────────────────────┐
│ 6. Create Output File        │
│    (FileUtils)                │
│    • Generate timestamp      │
│    • Create file path        │
└──────────────────────────────┘
    │
    ↓
┌──────────────────────────────┐
│ 7. Generate PDF              │
│    (PdfGenerator)             │
│    • Convert HTML to PDF     │
│    • Save to file            │
│    • Verify creation         │
└──────────────────────────────┘
    │
    ├─→ Failed ─→ Show Error ─→ End
    │
    ↓ Success
┌──────────────────────────────┐
│ 8. Show Success Message      │
│    (MainActivity)             │
│    • Display file path       │
│    • Enable UI               │
└──────────────────────────────┘
    │
    ↓
   End
```

## Error Handling Flow

```
┌─────────────────────────────────────────────┐
│         Conversion Operation                │
└─────────────────────────────────────────────┘
                    │
    ┌───────────────┼───────────────┐
    │               │               │
    ↓               ↓               ↓
┌─────────┐   ┌─────────┐   ┌─────────┐
│ Success │   │ Failure │   │Exception│
└─────────┘   └─────────┘   └─────────┘
    │               │               │
    ↓               ↓               ↓
┌─────────────────────────────────────────────┐
│         Sealed Class: ConversionResult      │
│                                             │
│  Success(outputPath: String)                │
│     ↓                                       │
│  Show: "PDF saved to: /path/to/file.pdf"   │
│                                             │
│  Failure(reason: String)                    │
│     ↓                                       │
│  Show specific error:                       │
│  • "File is too large (15MB)"              │
│  • "Cannot read file content"              │
│  • "Markdown parsing failed: ..."         │
│  • "External storage not available"        │
│  • "Insufficient storage space"            │
│  • "PDF generation failed"                 │
└─────────────────────────────────────────────┘
```

## Key Improvements Visualization

```
┌──────────────────────────────────────────────────────┐
│                Before → After                        │
├──────────────────────────────────────────────────────┤
│                                                      │
│  Code Organization:                                  │
│  1 file (305 lines) → 5 files (529 lines)           │
│  ████████████████████ → ████ ████ ████ ████ ████    │
│                                                      │
│  Error Messages:                                     │
│  2 generic → 10+ specific                            │
│  ██ → ██████████                                     │
│                                                      │
│  Validation:                                         │
│  None → File + Storage validation                    │
│  ∅ → ████████                                        │
│                                                      │
│  CSS Styling:                                        │
│  50 lines → 140 lines (180% increase)                │
│  ████████ → ████████████████████████                 │
│                                                      │
│  Documentation:                                      │
│  2 files → 7 files                                   │
│  ████ → ██████████████                               │
│                                                      │
│  Testability:                                        │
│  Hard → Easy                                         │
│  ██ → ██████████                                     │
│                                                      │
│  Maintainability:                                    │
│  Low → High                                          │
│  ███ → ██████████                                    │
│                                                      │
└──────────────────────────────────────────────────────┘
```

## Component Dependencies

```
                    MainActivity
                         │
         ┌───────────────┼───────────────┐
         │               │               │
         ↓               ↓               ↓
   FileValidator    MarkdownConverter  FileUtils
         │                              │
         │                              ↓
         │                         PdfGenerator
         │                              ↑
         └──────────────┬───────────────┘
                        │
                  (Uses context
                   for file I/O)
```

## Benefits Summary

```
✅ Modular Architecture
   • Easy to understand
   • Easy to test
   • Easy to extend

✅ Better Validation
   • File size checks
   • Storage checks
   • Early error detection

✅ Enhanced Errors
   • Specific messages
   • Type-safe handling
   • Better debugging

✅ Improved Output
   • Professional styling
   • Better tables
   • Enhanced typography

✅ Comprehensive Docs
   • Architecture guide
   • Enhancement guide
   • Change log
   • Summary
```

## Future Extension Points

```
Current Architecture
        │
        ├─→ Add Theme Selection
        │   (Extend MarkdownConverter)
        │
        ├─→ Add Batch Processing
        │   (Extend FileUtils)
        │
        ├─→ Add Cloud Storage
        │   (Extend FileValidator)
        │
        ├─→ Add Preview Mode
        │   (Reuse MarkdownConverter)
        │
        └─→ Add Custom Settings
            (New SettingsManager class)
```
