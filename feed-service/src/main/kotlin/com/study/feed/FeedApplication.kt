package com.study.feed

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["com.study.feed", "com.study.common"])
@EnableScheduling
class FeedApplication

fun main(args: Array<String>) {
    runApplication<FeedApplication>(*args)
}
