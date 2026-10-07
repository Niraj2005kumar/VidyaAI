package com.vidyanova.ai.ai.retrieval

class CurriculumRetriever(
    private val vectorStore: VectorStore
) {

    fun retrieve(
        question: String,
        classLevel: Int,
        subject: String,
        limit: Int = 5
    ): List<CurriculumChunk> {
        return vectorStore.search(
            query = question,
            classLevel = classLevel,
            subject = subject,
            limit = limit
        )
    }

    fun buildContext(
        question: String,
        classLevel: Int,
        subject: String,
        limit: Int = 5
    ): String {
        val results = retrieve(
            question = question,
            classLevel = classLevel,
            subject = subject,
            limit = limit
        )

        if (results.isEmpty()) {
            return ""
        }

        return buildString {
            appendLine("Relevant curriculum context:")

            results.forEachIndexed { index, chunk ->
                appendLine()
                appendLine("[$index]")
                appendLine("Chapter: ${chunk.chapter}")
                appendLine("Topic: ${chunk.topic}")
                appendLine("Content: ${chunk.content}")
            }
        }
    }
}