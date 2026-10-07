package com.vidyanova.ai.ai.model

object ModelConfig {

    const val MODEL_FILE_NAME = "tutor-model.gguf"


    const val MODEL_DISPLAY_NAME = "ViyaAI Qwen 1.5B"

    const val CONTEXT_SIZE = 2048

    const val THREAD_COUNT = 4

    const val BATCH_SIZE = 256

    const val TEMPERATURE = 0.7f

    const val TOP_P = 0.9f

    const val MAX_NEW_TOKENS = 256

    const val QUANTIZATION = "Q4_K_M"

    const val OFFLINE_MODE = true
}