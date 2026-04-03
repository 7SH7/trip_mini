package com.study.accommodation

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.study.accommodation", "com.study.common"])
class AccommodationApplication

fun main(args: Array<String>) {
    runApplication<AccommodationApplication>(*args)
}
