package com.vidyanova.ai.ai.retrieval

class EmbeddingEngine {

    private var initialized = false

    fun initialize(): Boolean {
        initialized = true
        return true
    }

    fun isInitialized(): Boolean {
        return initialized
    }

    fun embed(text: String): FloatArray {
        if (!initialized) {
            throw IllegalStateException(
                "Embedding engine is not initialized"
            )
        }

        if (text.isBlank()) {
            return FloatArray(0)
        }

        throw UnsupportedOperationException(
            "Sentence Transformer embedding model is not connected yet"
        )
    }

    fun embedAll(texts: List<String>): List<FloatArray> {
        return texts.map { text ->
            embed(text)
        }
    }

    fun close() {
        initialized = false
    }
}