package com.study.subscription

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["com.study.subscription", "com.study.common"])
@EnableScheduling
@EntityScan(basePackages = ["com.study.subscription", "com.study.common"])
@EnableJpaRepositories(basePackages = ["com.study.subscription", "com.study.common"])
class SubscriptionApplication

fun main(args: Array<String>) {
    runApplication<SubscriptionApplication>(*args)
}
