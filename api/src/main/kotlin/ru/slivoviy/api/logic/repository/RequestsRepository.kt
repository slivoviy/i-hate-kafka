package ru.slivoviy.api.logic.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.slivoviy.api.logic.entity.Request

@Repository
interface RequestsRepository : JpaRepository<Request, Long>