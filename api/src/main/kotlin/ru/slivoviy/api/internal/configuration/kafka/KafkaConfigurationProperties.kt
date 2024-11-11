package ru.slivoviy.api.internal.configuration.kafka

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "kafka")
class KafkaConfigurationProperties (
    val consumer: KafkaConsumerConfigurationProperties,
    val producer: KafkaProducerConfigurationProperties,
    val targetTopics: Map<String, TopicProperties>,
    val sourceTopics: Map<String, TopicProperties>,
)

data class KafkaConsumerConfigurationProperties(
    val bootstrapServers: String,
)

data class KafkaProducerConfigurationProperties(
    val bootstrapServers: String,
)

data class TopicProperties(
    val name: String,
    val concurrency: Int
)