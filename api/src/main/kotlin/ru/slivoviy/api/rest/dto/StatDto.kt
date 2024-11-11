package ru.slivoviy.api.rest.dto

data class StatDto(
    val framesTotal: Int,
    val framesDone: Int,
    val status: String,
    val results: List<PredictionDto>? = null,
)