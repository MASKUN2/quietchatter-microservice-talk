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

@SpringBootTest
@Testcontainers
class CacheIntegrationTest {

    companion object {
        @Container
        val redisContainer = GenericContainer("redis:7-alpine").withExposedPorts(6379)

        @JvmStatic
        @DynamicPropertySource
        fun redisProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.redis.host") { redisContainer.host }
            registry.add("spring.data.redis.port") { redisContainer.firstMappedPort }
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
