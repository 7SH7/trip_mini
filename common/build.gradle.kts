plugins {
    `java-library`
    kotlin("plugin.jpa")
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.kafka:spring-kafka")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.springframework.cloud:spring-cloud-starter-loadbalancer")
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    api("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6")
}
