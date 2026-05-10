package com.quietchatter.talk.adaptor.`in`.web

import com.quietchatter.talk.adaptor.out.external.InternalMemberResponse
import com.quietchatter.talk.adaptor.out.external.MemberClient
import com.quietchatter.talk.adaptor.out.persistence.TalkJpaRepository
import com.quietchatter.talk.domain.Talk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, topics = ["talk", "member"])
@TestPropertySource(properties = [
    "spring.cache.type=none",
    "spring.cloud.stream.kafka.binder.brokers=\${spring.embedded.kafka.brokers}"
])
class HiddenTalkIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var talkJpaRepository: TalkJpaRepository

    @MockitoBean
    private lateinit var memberClient: MemberClient

    private lateinit var memberId: UUID
    private lateinit var otherMemberId: UUID

    @BeforeEach
    fun setUp() {
        talkJpaRepository.deleteAll()
        memberId = UUID.randomUUID()
        otherMemberId = UUID.randomUUID()
        whenever(memberClient.getMemberInfo(any())).thenReturn(
            InternalMemberResponse(memberId, "tester")
        )
    }

    @Test
    fun `공개 톡 조회 - hidden 파라미터 없으면 visible 톡만 반환`() {
        talkJpaRepository.saveAll(listOf(
            Talk(UUID.randomUUID(), memberId, "tester", "visible talk"),
            Talk(UUID.randomUUID(), memberId, "tester", "hidden talk").apply { hide() }
        ))

        mockMvc.perform(
            get("/api/talks")
                .param("memberId", memberId.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].content").value("visible talk"))
    }

    @Test
    fun `공개 톡 조회 - hidden=false 이면 인증 없이도 visible 톡 반환`() {
        talkJpaRepository.save(Talk(UUID.randomUUID(), memberId, "tester", "visible talk"))

        mockMvc.perform(
            get("/api/talks")
                .param("memberId", memberId.toString())
                .param("hidden", "false")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
    }

    @Test
    fun `숨겨진 톡 조회 - 본인 요청이면 hidden 톡 반환`() {
        talkJpaRepository.saveAll(listOf(
            Talk(UUID.randomUUID(), memberId, "tester", "visible talk"),
            Talk(UUID.randomUUID(), memberId, "tester", "hidden talk").apply { hide() }
        ))

        mockMvc.perform(
            get("/api/talks")
                .param("memberId", memberId.toString())
                .param("hidden", "true")
                .header("X-Member-Id", memberId.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].content").value("hidden talk"))
    }

    @Test
    fun `숨겨진 톡 조회 - 타인 요청이면 403 반환`() {
        mockMvc.perform(
            get("/api/talks")
                .param("memberId", memberId.toString())
                .param("hidden", "true")
                .header("X-Member-Id", otherMemberId.toString())
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `숨겨진 톡 조회 - 비로그인 요청이면 403 반환`() {
        mockMvc.perform(
            get("/api/talks")
                .param("memberId", memberId.toString())
                .param("hidden", "true")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `숨김 해제 - 본인 요청이면 톡이 visible 상태로 복구됨`() {
        val hiddenTalk = talkJpaRepository.save(
            Talk(UUID.randomUUID(), memberId, "tester", "hidden talk").apply { hide() }
        )

        mockMvc.perform(
            post("/api/talks/{talkId}/restore", hiddenTalk.id)
                .header("X-Member-Id", memberId.toString())
        )
            .andExpect(status().isNoContent)

        val restored = talkJpaRepository.findById(hiddenTalk.id!!).orElseThrow()
        assert(!restored.isHidden)
        assert(restored.dateToHidden!!.isAfter(java.time.LocalDate.now()))
    }

    @Test
    fun `숨김 해제 - 타인 요청이면 403 반환`() {
        val hiddenTalk = talkJpaRepository.save(
            Talk(UUID.randomUUID(), memberId, "tester", "hidden talk").apply { hide() }
        )

        mockMvc.perform(
            post("/api/talks/{talkId}/restore", hiddenTalk.id)
                .header("X-Member-Id", otherMemberId.toString())
        )
            .andExpect(status().isForbidden)
    }
}
