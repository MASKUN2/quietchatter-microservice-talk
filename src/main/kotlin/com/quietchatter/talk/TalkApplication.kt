package com.quietchatter.talk

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
class TalkApplication

fun main(args: Array<String>) {
    runApplication<TalkApplication>(*args)
}
