package ru.slivoviy.runner.logic.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Table(name = "predictions")
@Entity
data class Prediction(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(cascade = [CascadeType.MERGE])
    @JoinColumn(name = "request_id", referencedColumnName = "id", nullable = false)
    val request: Request,

    @Column(name = "frame_id", nullable = false)
    val frameId: Int,

    @Column(name = "prediction", nullable = false)
    val prediction: String,
)
