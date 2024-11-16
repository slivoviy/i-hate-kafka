package ru.slivoviy.orchestrator.logic.repository

import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ru.slivoviy.orchestrator.logic.entity.Request

@Repository
interface RequestsRepository : JpaRepository<Request, Long> {
    @Transactional
    @Modifying
    @Query("update Request r set r.status = ?1 where r.id = ?2")
    fun updateStatusById(status: Int, id: Long): Long
}