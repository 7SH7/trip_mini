package com.study.booking

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.study.booking", "com.study.common"])
class BookingApplication

fun main(args: Array<String>) {
    runApplication<BookingApplication>(*args)
}
