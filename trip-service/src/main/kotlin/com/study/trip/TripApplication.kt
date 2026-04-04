package com.study.trip

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["com.study.trip", "com.study.common"])
@EnableScheduling
@EntityScan(basePackages = ["com.study.trip", "com.study.common"])
@EnableJpaRepositories(basePackages = ["com.study.trip", "com.study.common"])
class TripApplication

fun main(args: Array<String>) {
    runApplication<TripApplication>(*args)
}
