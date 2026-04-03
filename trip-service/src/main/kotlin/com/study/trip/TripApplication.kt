package com.study.trip

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.study.trip", "com.study.common"])
class TripApplication

fun main(args: Array<String>) {
    runApplication<TripApplication>(*args)
}
