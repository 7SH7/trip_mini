package com.study.booking

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["com.study.booking", "com.study.common"])
@EnableScheduling
@EntityScan(basePackages = ["com.study.booking", "com.study.common"])
@EnableJpaRepositories(basePackages = ["com.study.booking", "com.study.common"])
class BookingApplication

fun main(args: Array<String>) {
    runApplication<BookingApplication>(*args)
}
