package ru.slivoviy.api.logic.repository;

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.slivoviy.api.logic.entity.Prediction

interface PredictionRepository : JpaRepository<Prediction, Long> {
    @Query("select r from Prediction r where r.request.id = ?1")
    fun findResultsByRequest(id: Long): List<Prediction>

}