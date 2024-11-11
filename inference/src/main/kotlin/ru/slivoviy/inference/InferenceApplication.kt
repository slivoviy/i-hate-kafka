package ru.slivoviy.inference

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import ru.slivoviy.inference.internal.configuration.kafka.KafkaConfigurationProperties

@EnableConfigurationProperties(
    KafkaConfigurationProperties::class,
)
@SpringBootApplication
class InferenceApplication {
}

fun main(args: Array<String>) {
    runApplication<InferenceApplication>(*args)
}