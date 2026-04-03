package com.study.feed.infrastructure.storage

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.HeadBucketRequest
import software.amazon.awssdk.services.s3.model.NoSuchBucketException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.net.URI
import java.util.UUID

@Component
class S3StorageClient(
    @Value("\${s3.endpoint}") private val endpoint: String,
    @Value("\${s3.access-key}") private val accessKey: String,
    @Value("\${s3.secret-key}") private val secretKey: String,
    @Value("\${s3.bucket}") private val bucket: String,
    @Value("\${s3.region}") private val region: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val s3Client: S3Client by lazy {
        S3Client.builder()
            .endpointOverride(URI.create(endpoint))
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
            .region(Region.of(region))
            .forcePathStyle(true)
            .build()
    }

    @PostConstruct
    fun initBucket() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build())
            log.info("S3 bucket '{}' already exists", bucket)
        } catch (e: NoSuchBucketException) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build())
            log.info("Created S3 bucket '{}'", bucket)
        } catch (e: Exception) {
            log.warn("Could not check/create S3 bucket '{}': {}", bucket, e.message)
        }
    }

    fun upload(file: MultipartFile): String {
        val extension = file.originalFilename?.substringAfterLast('.', "jpg") ?: "jpg"
        val key = "feeds/${UUID.randomUUID()}.$extension"

        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.contentType ?: "image/jpeg")
                .build(),
            RequestBody.fromInputStream(file.inputStream, file.size)
        )

        log.info("Uploaded file to S3: {}", key)
        return "$endpoint/$bucket/$key"
    }
}
