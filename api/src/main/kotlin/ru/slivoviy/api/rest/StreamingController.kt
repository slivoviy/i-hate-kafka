package ru.slivoviy.api.rest

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.ResponseEntity
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import ru.slivoviy.api.internal.configuration.BUCKET_NAME
import ru.slivoviy.api.internal.configuration.kafka.KafkaConfigurationProperties
import ru.slivoviy.api.internal.configuration.kafka.PRODUCER_TEMPLATE
import ru.slivoviy.api.internal.extension.toDto
import ru.slivoviy.api.logic.entity.Request
import ru.slivoviy.api.logic.enum.Status
import ru.slivoviy.api.logic.enum.toInt
import ru.slivoviy.api.logic.enum.toStatus
import ru.slivoviy.api.logic.repository.PredictionRepository
import ru.slivoviy.api.logic.repository.RequestsRepository
import ru.slivoviy.api.rest.dto.StatDto
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer


private val logger = KotlinLogging.logger {}

@RestController
class StreamingController(
    private val requestsRepository: RequestsRepository,
    private val predictionRepository: PredictionRepository,
    private val s3Client: S3Client,
    @Qualifier(PRODUCER_TEMPLATE) private val kafkaTemplate: KafkaTemplate<String?, String?>,
    private val kafkaProperties: KafkaConfigurationProperties,
) {

    @PostMapping("api/v1/upload")
    fun uploadVideo(videoFile: MultipartFile): ResponseEntity<Long> {
        val request = requestsRepository.save(
            Request(
                framesDone = 0,
                framesTotal = 0,
                status = Status.INIT_STARTUP.toInt()
            )
        )

        uploadFile(request.id!!, videoFile)

        kafkaTemplate.send(
            kafkaProperties.targetTopics["Orchestrator-Api"]!!.name,
            "${request.id}"
        )

        return ResponseEntity.ok(1)
    }

    @GetMapping("api/v1/status/{id}")
    fun getStatus(@PathVariable("id") id: Long): ResponseEntity<StatDto> {
        val request = requestsRepository.findById(id).orElseThrow {
            throw Exception("Request with id [$id] not found")
        }

        val result =
            if (request.status.toStatus() == Status.ACTIVE ||
                request.status.toStatus() == Status.INACTIVE
            ) {
                predictionRepository.findResultsByRequest(id)
            } else null

        return ResponseEntity.ok(
            StatDto(
                framesTotal = request.framesTotal,
                framesDone = request.framesDone,
                status = request.status.toStatus().name,
                results = result?.map { it.toDto() }
            )
        )
    }

    private fun uploadFile(id: Long, videoFile: MultipartFile) {
        val keyName = "$id.mp4"

        val createRequest = CreateMultipartUploadRequest.builder()
            .bucket(BUCKET_NAME)
            .key(keyName)
            .build()
        val createResponse = s3Client.createMultipartUpload(createRequest)
        val uploadId = createResponse.uploadId()


        val completedParts: MutableList<CompletedPart> = ArrayList()
        var partNumber = 1
        val buffer: ByteBuffer =
            ByteBuffer.allocate(5 * 1024 * 1024)

        try {
            RandomAccessFile(videoFile.name, "r").use { file ->
                val fileSize: Long = file.length()
                var position: Long = 0
                while (position < fileSize) {
                    file.seek(position)
                    val bytesRead: Int = file.getChannel().read(buffer)

                    buffer.flip()
                    val uploadPartRequest = UploadPartRequest.builder()
                        .bucket(BUCKET_NAME)
                        .key(keyName)
                        .uploadId(uploadId)
                        .partNumber(partNumber)
                        .contentLength(bytesRead.toLong())
                        .build()

                    val response: UploadPartResponse =
                        s3Client.uploadPart(uploadPartRequest, RequestBody.fromByteBuffer(buffer))

                    completedParts.add(
                        CompletedPart.builder()
                            .partNumber(partNumber)
                            .eTag(response.eTag())
                            .build()
                    )

                    buffer.clear()
                    position += bytesRead.toLong()
                    partNumber++
                }

                val completedUpload = CompletedMultipartUpload.builder()
                    .parts(completedParts)
                    .build()

                val completeRequest = CompleteMultipartUploadRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(keyName)
                    .uploadId(uploadId)
                    .multipartUpload(completedUpload)
                    .build()

                s3Client.completeMultipartUpload(completeRequest)
            }
        } catch (e: IOException) {
            logger.error { "Error when trying to upload video to s3" }
        }
    }
}