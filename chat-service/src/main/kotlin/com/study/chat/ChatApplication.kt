package com.study.chat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["com.study.chat", "com.study.common"])
@EnableScheduling
@EntityScan(basePackages = ["com.study.chat", "com.study.common"])
@EnableJpaRepositories(basePackages = ["com.study.chat", "com.study.common"])
class ChatApplication

fun main(args: Array<String>) {
    runApplication<ChatApplication>(*args)
}
