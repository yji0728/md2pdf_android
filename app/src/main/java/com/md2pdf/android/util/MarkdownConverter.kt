package com.md2pdf.android.util

import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.commonmark.ext.gfm.tables.TablesExtension

/**
 * Utility class for converting Markdown to HTML
 */
object MarkdownConverter {
    
    /**
     * Converts markdown content to HTML with styling
     * @param markdownContent The markdown text to convert
     * @param fileName The name of the file being converted (used in title)
     * @return Full HTML document with CSS styling
     */
    fun convertToHtml(markdownContent: String, fileName: String?): String {
        if (markdownContent.isBlank()) {
            throw IllegalArgumentException("Markdown content cannot be empty")
        }
        
        // Parse markdown with GFM tables extension
        val extensions = listOf(TablesExtension.create())
        val parser = Parser.builder()
            .extensions(extensions)
            .build()
        
        val document = parser.parse(markdownContent)
        
        val htmlRenderer = HtmlRenderer.builder()
            .extensions(extensions)
            .build()
        
        val htmlContent = htmlRenderer.render(document)
        
        return createStyledHtml(htmlContent, fileName)
    }
    
    /**
     * Wraps HTML content in a full document with comprehensive CSS styling
     */
    private fun createStyledHtml(htmlContent: String, fileName: String?): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>${fileName ?: "Converted Document"}</title>
                <style>
                    ${getStyleSheet()}
                </style>
            </head>
            <body>
            $htmlContent
            </body>
            </html>
        """.trimIndent()
    }
    
    /**
     * Returns comprehensive CSS stylesheet for PDF output
     */
    private fun getStyleSheet(): String {
        return """
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
                font-weight: 600;
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
                background-color: #f8f9fa;
                padding: 12px 12px 12px 20px;
                border-radius: 4px;
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
                background-color: #3498db;
                color: white;
                font-weight: 600;
            }
            tr:nth-child(even) {
                background-color: #f8f9fa;
            }
            tr:hover {
                background-color: #ecf0f1;
            }
            ul, ol {
                padding-left: 24px;
                margin: 16px 0;
            }
            li {
                margin: 6px 0;
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
                font-weight: 600;
            }
            em {
                color: #7f8c8d;
            }
            hr {
                border: none;
                border-top: 2px solid #bdc3c7;
                margin: 24px 0;
            }
            img {
                max-width: 100%;
                height: auto;
                display: block;
                margin: 16px 0;
            }
        """.trimIndent()
    }
}
