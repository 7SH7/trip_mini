package com.study.media

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["com.study.media", "com.study.common"])
@EnableScheduling
@EnableAsync
@EntityScan(basePackages = ["com.study.media", "com.study.common"])
@EnableJpaRepositories(basePackages = ["com.study.media", "com.study.common"])
class MediaApplication

fun main(args: Array<String>) {
    runApplication<MediaApplication>(*args)
}
