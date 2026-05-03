package com.quietchatter.talk.application

import com.quietchatter.talk.application.out.TalkLoadable
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName

@SpringBootTest
@Testcontainers
class CacheIntegrationTest {

    companion object {
        @Container
        val redisContainer = GenericContainer("redis:7-alpine").withExposedPorts(6379)

        @Container
        val kafkaContainer = KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"))

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.redis.host") { redisContainer.host }
            registry.add("spring.data.redis.port") { redisContainer.firstMappedPort }
            registry.add("spring.cloud.stream.kafka.binder.brokers") { kafkaContainer.bootstrapServers }
        }
    }

    @Autowired
    lateinit var talkLoadable: TalkLoadable

    @Autowired
    lateinit var cacheManager: CacheManager

    @Test
    fun testCacheSerialization() {
        // Just call it and see if it throws ClassCastException or connection error
        val talks = talkLoadable.findRecommended(5)
        println("Loaded recommended talks: ${talks.size}")
    }
}
