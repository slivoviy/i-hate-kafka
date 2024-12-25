package ru.slivoviy.runner.internal.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

const val BUCKET_NAME = "slivoviy-videos"

@Configuration
class S3Configuration {

    @Bean
    fun s3Client(): S3Client {
        val accessKey = "minioadmi"
        val secretKey = "minioadmin"
        val credentials = AwsBasicCredentials.create(accessKey, secretKey)
        val regionName = "ru-central1"

        return S3Client
            .builder()
            .region(Region.of(regionName))
            .endpointOverride(URI("http://127.0.0.1:9000"))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build()
    }
}