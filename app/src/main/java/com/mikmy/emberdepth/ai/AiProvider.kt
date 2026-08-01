package com.mikmy.emberdepth.ai

interface AiProvider {
    val name: String
    val available: Boolean
    suspend fun complete(prompt: String, maxTokens: Int): Result<String>
}

class NoOpProvider : AiProvider {
    override val name = "none"
    override val available = false
    override suspend fun complete(prompt: String, maxTokens: Int) =
        Result.failure<String>(UnsupportedOperationException("AI not available in v1"))
}
