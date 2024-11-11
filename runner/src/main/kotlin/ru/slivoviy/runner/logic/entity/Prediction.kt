package ru.slivoviy.runner.logic.entity

import jakarta.persistence.*

@Table(name = "results")
@Entity
data class Prediction(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int? = null,

    @ManyToOne(cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH])
    @JoinColumn(name = "request_id", referencedColumnName = "id", nullable = false)
    val request: Request,

    @Column(name = "frame_id", nullable = false)
    val frameId: Int,

    @Column(name = "prediction", nullable = false)
    val prediction: String,
)
