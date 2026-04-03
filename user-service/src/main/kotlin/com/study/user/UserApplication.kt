package com.study.user

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.study.user", "com.study.common"])
class UserApplication

fun main(args: Array<String>) {
    runApplication<UserApplication>(*args)
}
