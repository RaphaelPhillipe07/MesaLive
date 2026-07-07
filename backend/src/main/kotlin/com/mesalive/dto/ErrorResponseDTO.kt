package com.mesalive.dto

import java.time.OffsetDateTime

data class ErrorResponseDTO(
    val status: Int,
    val error: String,
    val message: String,
    val timestamp: OffsetDateTime = OffsetDateTime.now(),
    val fieldErrors: Map<String, String>? = null
)
