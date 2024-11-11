package ru.slivoviy.runner.internal.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client

const val BUCKET_NAME = "videos"

@Configuration
class S3Configuration {

    @Bean
    fun s3Client(): S3Client {
        val accessKey = "access"
        val secretKey = "secret"
        val credentials = AwsBasicCredentials.create(accessKey, secretKey)

        val regionName = "region"

        val s3Client = S3Client
            .builder()
            .region(Region.of(regionName))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build()

        s3Client.createBucket { request -> request.bucket(BUCKET_NAME) }

        return s3Client
    }
}