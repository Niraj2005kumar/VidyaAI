package com.vidyanova.ai.ai.model

object ModelConfig {

    // Model information (Qwen 2.5 0.5B Instruct GGUF)
    const val MODEL_FILE_NAME = "tutor-model.gguf"
    const val MODEL_DISPLAY_NAME = "ViyaAI Qwen 0.5B"

    // Inference configuration
    const val CONTEXT_SIZE = 768
    const val BATCH_SIZE = 128
    const val UBATCH_SIZE = 128

    val THREAD_COUNT: Int
        get() = if (Runtime.getRuntime().availableProcessors() <= 4) 3 else 4

    // Response configuration
    const val MAX_NEW_TOKENS = 160
    const val TEMPERATURE = 0.7f
    const val TOP_P = 0.9f

    // Model configuration
    const val QUANTIZATION = "Q4_K_M"
    const val OFFLINE_MODE = true
}
