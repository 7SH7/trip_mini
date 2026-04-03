package com.study.booking

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["com.study.booking", "com.study.common"])
@EnableScheduling
class BookingApplication

fun main(args: Array<String>) {
    runApplication<BookingApplication>(*args)
}
