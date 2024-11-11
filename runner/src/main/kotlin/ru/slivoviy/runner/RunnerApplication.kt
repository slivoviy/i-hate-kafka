package ru.slivoviy.runner

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import ru.slivoviy.runner.internal.configuration.kafka.KafkaConfigurationProperties

@EnableConfigurationProperties(
    KafkaConfigurationProperties::class,
)
@SpringBootApplication
class RunnerApplication {
}

fun main(args: Array<String>) {
    runApplication<RunnerApplication>(*args)
}