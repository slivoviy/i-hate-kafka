package ru.slivoviy.inference.logic

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.jpa.domain.AbstractPersistable_
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import ru.slivoviy.inference.internal.configuration.kafka.CONSUMER_FACTORY
import ru.slivoviy.inference.internal.configuration.kafka.KafkaConfigurationProperties
import ru.slivoviy.inference.internal.configuration.kafka.PRODUCER_TEMPLATE


private val logger = KotlinLogging.logger { }

const val INFERENCE_LISTENER_ID = "inference"

val PREDICTIONS = listOf(
    "1 dog, 2 bicycles, 4 trees.",
    "3 cats, 1 bench, 2 birds.",
    "5 cars, 2 stop signs, 1 pedestrian crossing.",
    "4 people, 1 motorcycle, 6 streetlights.",
    "2 trucks, 1 traffic cone, 3 road signs.",
    "1 bus, 2 taxis, 4 shop fronts.",
    "3 houses, 1 mailbox, 2 hydrants.",
    "7 chairs, 1 table, 3 potted plants.",
    "2 boats, 1 jet ski, 4 buoys.",
    "5 runners, 1 football, 2 goalposts.",
    "4 squirrels, 1 park bench, 5 trees.",
    "2 tennis players, 1 net, 3 tennis balls.",
    "6 ducks, 1 pond, 3 benches.",
    "1 airplane, 2 luggage carts, 3 directional signs.",
    "2 swimmers, 1 pool, 4 pool lanes.",
    "3 children, 1 swing set, 2 slides.",
    "1 cow, 2 horses, 4 fences.",
    "2 street performers, 1 guitar, 3 hats.",
    "1 excavator, 2 dump trucks, 3 pylons.",
    "5 fish, 2 corals, 1 scuba diver.",
)
@Service
class InferenceService(
    @Qualifier(PRODUCER_TEMPLATE) private val kafkaTemplate: KafkaTemplate<String?, String?>,
    private val kafkaProperties: KafkaConfigurationProperties,
    ) {

    @KafkaListener(
        id = INFERENCE_LISTENER_ID,
        idIsGroup = false,
        containerFactory = CONSUMER_FACTORY,
        topics = ["\${kafka.source-topics.Inference.name}"],
        concurrency = "1"
    )
    private fun consumeRunner(
        @Payload record: String,
        @Header(KafkaHeaders.GROUP_ID) groupId: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
    ) {
        logger.debug { "Received message with groupId: [$groupId] partition: [$partition], and offset: [$offset]" }

//        "$id $counter $totalFrames ${frameBytes.toString(Charsets.UTF_8)}"
        val data = record.split(';')
        val requestId = data[0].toInt()
        val framesDone = data[1].toInt()
        val framesTotal = data[2].toInt()
        val frame = data[3].toByteArray()

        kafkaTemplate.send(
            kafkaProperties.targetTopics["Runner-Inference"]!!.name,
            "${requestId};$framesDone;$framesDone;$framesTotal;${PREDICTIONS.random()}"
        )

    }
}