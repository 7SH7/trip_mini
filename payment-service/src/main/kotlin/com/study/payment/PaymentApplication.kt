package com.study.payment

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["com.study.payment", "com.study.common"])
@EnableScheduling
@EntityScan(basePackages = ["com.study.payment", "com.study.common"])
@EnableJpaRepositories(basePackages = ["com.study.payment", "com.study.common"])
class PaymentApplication

fun main(args: Array<String>) {
    runApplication<PaymentApplication>(*args)
}
