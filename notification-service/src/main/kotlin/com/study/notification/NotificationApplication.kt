package com.study.notification

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["com.study.notification", "com.study.common"])
@EntityScan(basePackages = ["com.study.notification", "com.study.common"])
@EnableJpaRepositories(basePackages = ["com.study.notification", "com.study.common"])
class NotificationApplication

fun main(args: Array<String>) {
    runApplication<NotificationApplication>(*args)
}
