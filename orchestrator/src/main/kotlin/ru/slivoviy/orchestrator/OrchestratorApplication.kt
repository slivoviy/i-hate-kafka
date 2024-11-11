package ru.slivoviy.orchestrator

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import ru.slivoviy.orchestrator.internal.configuration.kafka.KafkaConfigurationProperties

@EnableConfigurationProperties(
    KafkaConfigurationProperties::class,
)
@SpringBootApplication
class OrchestratorApplication {
}

fun main(args: Array<String>) {
    runApplication<OrchestratorApplication>(*args)
}