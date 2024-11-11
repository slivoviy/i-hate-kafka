package ru.slivoviy.api.internal.configuration.kafka

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

const val PRODUCER_CONFIGURATION = "producerConfiguration"
const val PRODUCER_HEALTH_CONFIG = "producerHealthCofig"
const val PRODUCER_FACTORY = "producerFactory"
const val PRODUCER_TEMPLATE = "producerTemplate"


@Configuration
class KafkaConfiguration(
    @Autowired private val kafkaProperties: KafkaConfigurationProperties,
) {
    @Bean(PRODUCER_CONFIGURATION)
    fun producerConfiguration(): Map<String, Any> = mapOf(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.producer.bootstrapServers,
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
    )

    @Bean(PRODUCER_HEALTH_CONFIG)
    fun producerHealthConfig(
        @Qualifier(PRODUCER_CONFIGURATION) config: Map<String, Any>,
    ): Map<String, Any> = mapOf(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to (config[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] ?: ""),
        CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to (config[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] ?: ""),
        SaslConfigs.SASL_MECHANISM to (config[SaslConfigs.SASL_MECHANISM] ?: ""),
        SaslConfigs.SASL_JAAS_CONFIG to (config[SaslConfigs.SASL_JAAS_CONFIG] ?: ""),
    )

    @Bean(PRODUCER_FACTORY)
    fun producerFactory(
        @Qualifier(PRODUCER_CONFIGURATION) producerConfig: Map<String, Any>
    ): ProducerFactory<String?, String?> = DefaultKafkaProducerFactory(producerConfig)

    @Bean(PRODUCER_TEMPLATE)
    fun producerTemplate(
        @Qualifier(PRODUCER_FACTORY) producerFactory: ProducerFactory<String?, String?>
    ): KafkaTemplate<String?, String?> = KafkaTemplate(producerFactory)
}