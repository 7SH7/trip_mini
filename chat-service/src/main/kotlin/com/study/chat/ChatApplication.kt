package com.study.chat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["com.study.chat", "com.study.common"])
@EnableScheduling
class ChatApplication

fun main(args: Array<String>) {
    runApplication<ChatApplication>(*args)
}
