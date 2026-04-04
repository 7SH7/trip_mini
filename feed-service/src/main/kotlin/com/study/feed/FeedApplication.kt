package com.study.feed

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["com.study.feed", "com.study.common"])
@EnableScheduling
@EntityScan(basePackages = ["com.study.feed", "com.study.common"])
@EnableJpaRepositories(basePackages = ["com.study.feed", "com.study.common"])
class FeedApplication

fun main(args: Array<String>) {
    runApplication<FeedApplication>(*args)
}
