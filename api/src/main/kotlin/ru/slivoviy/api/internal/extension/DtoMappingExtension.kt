package ru.slivoviy.api.internal.extension

import ru.slivoviy.api.rest.dto.PredictionDto
import ru.slivoviy.api.logic.entity.Prediction

fun Prediction.toDto() =
    PredictionDto(
        frameId = frameId,
        prediction = prediction
    )
