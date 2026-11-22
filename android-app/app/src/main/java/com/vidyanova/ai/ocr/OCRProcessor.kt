package com.vidyanova.ai.ocr

object OCRProcessor {

    fun cleanText(rawText: String): String {
        if (rawText.isBlank()) {
            return ""
        }

        return rawText.split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString("\n")
    }

    fun getEmptyTextErrorMessage(): String {
        return "No clear text found in the image. Please try taking a clearer photo with good lighting."
    }
}
