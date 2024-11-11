package ru.slivoviy.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import ru.slivoviy.api.internal.configuration.kafka.KafkaConfigurationProperties

@EnableConfigurationProperties(
    KafkaConfigurationProperties::class,
)
@SpringBootApplication
class ApiApplication

fun main(args: Array<String>) {
    runApplication<ApiApplication>(*args)
}
