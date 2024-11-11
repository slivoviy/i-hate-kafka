package ru.slivoviy.runner.internal.configuration.kafka

import mu.KotlinLogging
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ContainerProperties.AckMode
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer

const val CONSUMER_CONFIGURATION = "consumerConfiguration"
const val CONSUMER_HEALTH_CONFIG = "consumerHealthCofig"
const val CONSUMER_FACTORY = "consumerFactory"
const val CONSUMER_VALUE_DESERIALIZER = "consumerValueDeserializer"

const val PRODUCER_CONFIGURATION = "producerConfiguration"
const val PRODUCER_HEALTH_CONFIG = "producerHealthCofig"
const val PRODUCER_FACTORY = "producerFactory"
const val PRODUCER_TEMPLATE = "producerTemplate"

private val logger = KotlinLogging.logger { }

@EnableKafka
@Configuration
class KafkaConsumerConfiguration(
    @Autowired private val kafkaProperties: KafkaConfigurationProperties,
) {

    @Bean(CONSUMER_CONFIGURATION)
    fun consumerConfiguration(): Map<String, Any> = mapOf(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.consumer.bootstrapServers,
        ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG to true,
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
    )

    @Bean(CONSUMER_HEALTH_CONFIG)
    fun consumerHealthConfig(
        @Qualifier(CONSUMER_CONFIGURATION) config: Map<String, Any>
    ): Map<String, Any> = mapOf(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to (config[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] ?: ""),
        CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to (config[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] ?: ""),
        SaslConfigs.SASL_MECHANISM to (config[SaslConfigs.SASL_MECHANISM] ?: ""),
        SaslConfigs.SASL_JAAS_CONFIG to (config[SaslConfigs.SASL_JAAS_CONFIG] ?: ""),
    )

    @Bean(CONSUMER_VALUE_DESERIALIZER)
    fun valueDeserializer(): StringDeserializer = StringDeserializer()

    @Bean(CONSUMER_FACTORY)
    fun consumerFactory(
        @Qualifier(CONSUMER_VALUE_DESERIALIZER) valueDeserializer: StringDeserializer,
        @Qualifier(CONSUMER_CONFIGURATION) consumerConfig: Map<String, Any>,
    ): ConcurrentKafkaListenerContainerFactory<String?, String> {
        return ConcurrentKafkaListenerContainerFactory<String?, String>().apply {
            consumerFactory = DefaultKafkaConsumerFactory(
                consumerConfig,
                ErrorHandlingDeserializer(StringDeserializer()),
                ErrorHandlingDeserializer(valueDeserializer).apply {
                    setFailedDeserializationFunction {
                        logger.error { "Deserialization failed for <${it.topic}> topic" }
                        null
                    }
                }
            )
            containerProperties.ackMode = AckMode.MANUAL
        }
    }
}

@Configuration
class KafkaProducerConfiguration(
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