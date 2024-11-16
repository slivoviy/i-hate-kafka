package ru.slivoviy.runner.logic.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import ru.slivoviy.runner.logic.entity.Request

@Repository
interface RequestsRepository : JpaRepository<Request, Long> {

    @Transactional
    @Modifying
    @Query("update Request r set r.framesTotal = ?1 where r.id = ?2")
    fun updateTotalFramesById(framesTotal: Int, id: Long)


    @Transactional
    @Modifying
    @Query("UPDATE requests " +
            "SET framesDone = :framesDone " +
            "WHERE id = :id " +
            "RETURNING *", nativeQuery = true)
    fun updateFramesDoneById(framesDone: Int, id: Long): Request
}