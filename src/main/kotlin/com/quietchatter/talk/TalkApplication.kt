package com.quietchatter.talk

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.web.config.EnableSpringDataWebSupport
import org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO
import org.springframework.scheduling.annotation.EnableScheduling

@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@EnableScheduling
@EnableFeignClients
@SpringBootApplication
class TalkApplication

fun main(args: Array<String>) {
    runApplication<TalkApplication>(*args)
}
