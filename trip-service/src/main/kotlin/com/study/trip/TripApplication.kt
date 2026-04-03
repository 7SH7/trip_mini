package com.study.trip

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["com.study.trip", "com.study.common"])
@EnableScheduling
class TripApplication

fun main(args: Array<String>) {
    runApplication<TripApplication>(*args)
}
