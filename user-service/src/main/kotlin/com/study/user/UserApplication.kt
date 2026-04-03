package com.study.user

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["com.study.user", "com.study.common"])
@EnableScheduling
class UserApplication

fun main(args: Array<String>) {
    runApplication<UserApplication>(*args)
}
