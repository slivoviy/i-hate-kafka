package ru.slivoviy.orchestrator.logic.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import ru.slivoviy.orchestrator.internal.configuration.kafka.CONSUMER_FACTORY
import ru.slivoviy.orchestrator.internal.configuration.kafka.KafkaConfigurationProperties
import ru.slivoviy.orchestrator.internal.configuration.kafka.PRODUCER_TEMPLATE
import ru.slivoviy.orchestrator.logic.enum.Status
import ru.slivoviy.orchestrator.logic.enum.toInt
import ru.slivoviy.orchestrator.logic.repository.RequestsRepository

private val logger = KotlinLogging.logger { }

const val ORCHESTRATOR_API_LISTENER_ID = "orchestrator-api"
const val ORCHESTRATOR_RUNNER_LISTENER_ID = "orchestrator-runner"

@Service
class OrchestratorService(
    @Qualifier(PRODUCER_TEMPLATE) private val kafkaTemplate: KafkaTemplate<String?, String?>,
    private val kafkaProperties: KafkaConfigurationProperties,
    private val requestsRepository: RequestsRepository,
    ) {

    @KafkaListener(
        id = ORCHESTRATOR_API_LISTENER_ID,
        groupId = "orchestrator1",
        containerFactory = CONSUMER_FACTORY,
        topics = ["\${kafka.source-topics.Api.name}"],
        concurrency = "1"
    )
    private fun consumeApi(
        @Payload record: String,
        @Header(KafkaHeaders.GROUP_ID) groupId: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
    ) {
        logger.debug { "Received message with groupId: [$groupId] partition: [$partition], and offset: [$offset]" }

        requestsRepository.updateStatusById(Status.IN_STARTUP_PROCESSING.toInt(), record.toLong())

        sendToRunner(record)
    }

    private fun sendToRunner(record: String) {
        kafkaTemplate.send(
            kafkaProperties.targetTopics["Runner-Orchestrator"]!!.name,
            record
        )
    }

    @KafkaListener(
        id = ORCHESTRATOR_RUNNER_LISTENER_ID,
        groupId = "orchestrator2",
        containerFactory = CONSUMER_FACTORY,
        topics = ["\${kafka.source-topics.Runner-Orchestrator.name}"],
        concurrency = "1"
    )
    private fun consumeRunner(
        @Payload record: String,
        @Header(KafkaHeaders.GROUP_ID) groupId: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
    ) {
        logger.debug { "Received message with groupId: [$groupId] partition: [$partition], and offset: [$offset]" }
//        "id status frames_done frames_total"
        val data = record.split(' ')
        val id = data[0].toLong()
        val status = Status.valueOf(data[1])
        val framesDone = data[2].toInt()
        val framesTotal = data[3].toInt()

        if (framesDone == framesTotal) requestsRepository.updateStatusById(Status.INACTIVE.toInt(), id)
        if (status == Status.IN_STARTUP_PROCESSING) requestsRepository.updateStatusById(Status.ACTIVE.toInt(), id)
    }
}