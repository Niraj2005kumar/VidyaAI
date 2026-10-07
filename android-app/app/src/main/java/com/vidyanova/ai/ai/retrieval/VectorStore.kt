package com.vidyanova.ai.ai.retrieval

data class CurriculumChunk(
    val id: String,
    val classLevel: Int,
    val subject: String,
    val chapter: String,
    val topic: String,
    val content: String
)

class VectorStore {

    private val chunks = mutableListOf<CurriculumChunk>()

    fun add(chunk: CurriculumChunk) {
        chunks.add(chunk)
    }

    fun addAll(items: List<CurriculumChunk>) {
        chunks.addAll(items)
    }

    fun clear() {
        chunks.clear()
    }

    fun size(): Int {
        return chunks.size
    }

    fun search(
        query: String,
        classLevel: Int,
        subject: String,
        limit: Int = 5
    ): List<CurriculumChunk> {
        if (query.isBlank()) {
            return emptyList()
        }

        val normalizedQuery = query.lowercase()

        return chunks
            .asSequence()
            .filter {
                it.classLevel == classLevel &&
                    it.subject.equals(
                        subject,
                        ignoreCase = true
                    )
            }
            .map { chunk ->
                val text = listOf(
                    chunk.chapter,
                    chunk.topic,
                    chunk.content
                ).joinToString(" ").lowercase()

                val words = normalizedQuery
                    .split(Regex("\\s+"))
                    .filter { it.length > 2 }

                val score = words.count {
                    text.contains(it)
                }

                chunk to score
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
            .toList()
    }
}