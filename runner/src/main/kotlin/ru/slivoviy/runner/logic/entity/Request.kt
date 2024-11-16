package ru.slivoviy.runner.logic.entity

import jakarta.persistence.*

@Table(name = "requests")
@Entity
data class Request(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "frames_total", nullable = false)
    var framesTotal: Int,

    @Column(name = "frames_done", nullable = false)
    var framesDone: Int,

    @Column(name = "status", nullable = false)
    var status: Int,
)