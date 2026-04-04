package com.study.media.infrastructure.storage

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import java.io.File
import java.net.URI
import java.nio.file.Files

@Component
class S3StorageClient(
    @Value("\${s3.endpoint}") private val endpoint: String,
    @Value("\${s3.access-key}") private val accessKey: String,
    @Value("\${s3.secret-key}") private val secretKey: String,
    @Value("\${s3.bucket}") private val bucket: String,
    @Value("\${s3.region}") private val region: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    val s3Client: S3Client by lazy {
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
        } catch (e: NoSuchBucketException) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build())
            log.info("Created S3 bucket '{}'", bucket)
        } catch (e: Exception) {
            log.warn("Could not check/create S3 bucket '{}': {}", bucket, e.message)
        }
    }

    fun download(s3Key: String, targetFile: File) {
        Files.createDirectories(targetFile.parentFile.toPath())
        s3Client.getObject(
            GetObjectRequest.builder().bucket(bucket).key(s3Key).build(),
            targetFile.toPath()
        )
        log.info("Downloaded S3 object {} to {}", s3Key, targetFile.absolutePath)
    }

    fun upload(localFile: File, s3Key: String, contentType: String = "application/octet-stream") {
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .contentType(contentType)
                .build(),
            RequestBody.fromFile(localFile)
        )
        log.info("Uploaded file to S3: {}", s3Key)
    }

    fun getPublicUrl(s3Key: String): String = "$endpoint/$bucket/$s3Key"
}
