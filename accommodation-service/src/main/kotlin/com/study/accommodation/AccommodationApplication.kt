package com.study.accommodation

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["com.study.accommodation", "com.study.common"])
@EntityScan(basePackages = ["com.study.accommodation", "com.study.common"])
@EnableJpaRepositories(basePackages = ["com.study.accommodation", "com.study.common"])
class AccommodationApplication

fun main(args: Array<String>) {
    runApplication<AccommodationApplication>(*args)
}
