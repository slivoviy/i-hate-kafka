package ru.slivoviy.orchestrator.logic.enum

enum class Status {
    INIT_STARTUP,
    IN_STARTUP_PROCESSING,
    INIT_SHUTDOWN,
    IN_SHUTDOWN_PROCESSING,
    ACTIVE,
    INACTIVE,
}

fun Status.toInt() =
    when (this) {
        Status.INIT_STARTUP -> 1
        Status.IN_STARTUP_PROCESSING -> 2
        Status.INIT_SHUTDOWN -> 3
        Status.IN_SHUTDOWN_PROCESSING -> 4
        Status.ACTIVE -> 5
        Status.INACTIVE -> 6
    }

fun Int.toStatus() =
    when (this) {
        1 -> Status.INIT_STARTUP
        2 -> Status.IN_STARTUP_PROCESSING
        3 -> Status.INIT_SHUTDOWN
        4 -> Status.IN_SHUTDOWN_PROCESSING
        5 -> Status.ACTIVE
        6 -> Status.INACTIVE
        else -> throw Exception("Unknow Status value = [$this]")
    }