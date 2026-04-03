package com.study.media

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication(scanBasePackages = ["com.study.media", "com.study.common"])
@EnableScheduling
@EnableAsync
class MediaApplication

fun main(args: Array<String>) {
    runApplication<MediaApplication>(*args)
}
