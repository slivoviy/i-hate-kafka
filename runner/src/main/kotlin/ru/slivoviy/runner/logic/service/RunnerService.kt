package ru.slivoviy.runner.logic.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import ru.slivoviy.runner.internal.configuration.BUCKET_NAME
import ru.slivoviy.runner.internal.configuration.kafka.CONSUMER_FACTORY
import ru.slivoviy.runner.internal.configuration.kafka.KafkaConfigurationProperties
import ru.slivoviy.runner.internal.configuration.kafka.PRODUCER_TEMPLATE
import ru.slivoviy.runner.logic.entity.Prediction
import ru.slivoviy.runner.logic.repository.PredictionRepository
import ru.slivoviy.runner.logic.repository.RequestsRepository
import software.amazon.awssdk.services.s3.S3Client
import java.io.FileOutputStream


private val logger = KotlinLogging.logger { }

const val RUNNER_ORCHESTRATOR_LISTENER_ID = "runner-orchestrator"
const val RUNNER_INFERENCE_LISTENER_ID = "runner-inference"

@Service
class OrchestratorService(
    @Qualifier(PRODUCER_TEMPLATE) private val kafkaTemplate: KafkaTemplate<String?, String?>,
    private val kafkaProperties: KafkaConfigurationProperties,
    private val requestsRepository: RequestsRepository,
    private val s3Client: S3Client, private val predictionRepository: PredictionRepository,
) {

    @KafkaListener(
        id = RUNNER_ORCHESTRATOR_LISTENER_ID,
        groupId = "runner1",
        containerFactory = CONSUMER_FACTORY,
        topics = ["\${kafka.source-topics.Runner-Orchestrator.name}"],
        concurrency = "1"
    )
    private fun consumeOrchestrator(
        @Payload record: String,
        @Header(KafkaHeaders.GROUP_ID) groupId: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
    ) {
        logger.debug { "Received message with groupId: [$groupId] partition: [$partition], and offset: [$offset]" }


        CoroutineScope(Dispatchers.IO).launch {
            processVideo(record)
        }
    }

    fun processVideo(id: String) {
        println("id: $id")

        val response = s3Client.getObject { request ->
            request
                .bucket(BUCKET_NAME)
                .key("$id.mp4")
        }

        FileOutputStream("$id.mp4").use { fos -> fos.write(response.readAllBytes()) }
//        OpenCV.loadLibrary()
//
//        val capture = VideoCapture("$id.mp4")
//        val frame = Mat()
//        var counter = 0

        val totalFrames = (1440..1680).random()
        requestsRepository.updateTotalFramesById(totalFrames, id.toLong())

        for (i in (0..totalFrames)) {
            val frameBytes = generateByteArray()

            kafkaTemplate.send(
                kafkaProperties.targetTopics["Inference-Runner"]!!.name,
                "$id $i $totalFrames ${frameBytes.toString(Charsets.UTF_8)}"
            )
        }

//        while (capture.isOpened) {
//
//            val ok = capture.read(frame)
//
//            if(ok) {
//                val size = frame.total() * frame.elemSize()
//                val frameBytes = ByteArray(size = size.toInt())
//                frame.get(0, 0, frameBytes)
//                counter++
//
//
//            }
//        }

    }

    @KafkaListener(
        id = RUNNER_INFERENCE_LISTENER_ID,
        groupId = "runner2",
        containerFactory = CONSUMER_FACTORY,
        topics = ["\${kafka.source-topics.Runner-Inference.name}"],
        concurrency = "1"
    )
    private fun consumeInference(
        @Payload record: String,
        @Header(KafkaHeaders.GROUP_ID) groupId: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long,
    ) {
        logger.debug { "Received message with groupId: [$groupId] partition: [$partition], and offset: [$offset]" }

//        "request_id;frame_id;counter;total_frames;prediction"
        val data = record.split(';')
        val requestId = data[0].toLong()
        val frameId = data[1].toInt()
        val framesDone = data[2].toInt()
        val framesTotal = data[3].toInt()
        val prediction = data[4]

        val request = requestsRepository.updateFramesDoneById(framesDone, requestId)
        predictionRepository.save(
            Prediction(
                request = request,
                frameId = frameId,
                prediction = prediction
            )
        )

//        "id status frames_done frames_total"
        kafkaTemplate.send(
            kafkaProperties.targetTopics["Orchestrator-Runner"]!!.name,
            "$requestId ${request.status} $framesDone $framesTotal"
        )
    }
}

private fun generateByteArray() =
    (0..720).map { (0..255).random().toByte() }.toByteArray()