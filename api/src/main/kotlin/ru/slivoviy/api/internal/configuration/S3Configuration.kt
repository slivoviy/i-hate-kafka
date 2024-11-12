package ru.slivoviy.api.internal.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

const val BUCKET_NAME = "slivoviy.videos"

@Configuration
class S3Configuration {

    @Bean
    fun s3Client(): S3Client {
        val accessKey = "YCAJEvmlGpNpfPt8SEVuWz1Vu"
        val secretKey = "YCN2pck0d__TkECL1pM8jY1UPMy9FUM2-0f8rERW"
        val credentials = AwsBasicCredentials.create(accessKey, secretKey)

        val regionName = "ru-central1"

//        s3Client.createBucket { request -> request.bucket(BUCKET_NAME) }

        return S3Client
            .builder()
            .region(Region.of(regionName))
            .endpointOverride(URI.create("https://storage.yandexcloud.net"))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build()
    }
}