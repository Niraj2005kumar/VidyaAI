
package com.vidyanova.ai.ai.model

object ModelConfig {

    // Model information
    const val MODEL_FILE_NAME = "tutor-model.gguf"
    const val MODEL_DISPLAY_NAME = "ViyaAI Qwen 1.5B"

    // Inference configuration
    const val CONTEXT_SIZE = 1024
    const val THREAD_COUNT = 2
    const val BATCH_SIZE = 64

    // Response configuration
    const val MAX_NEW_TOKENS = 64
    const val TEMPERATURE = 0.7f
    const val TOP_P = 0.9f

    // Model configuration
    const val QUANTIZATION = "Q4_K_M"
    const val OFFLINE_MODE = true
}
