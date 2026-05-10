package com.quietchatter.talk.adaptor.`in`.web.error

import com.quietchatter.talk.adaptor.`in`.web.ReactionController
import com.quietchatter.talk.adaptor.`in`.web.TalkController
import com.quietchatter.talk.application.`in`.ReactionModifiable
import com.quietchatter.talk.application.`in`.TalkCommandable
import com.quietchatter.talk.application.`in`.TalkQueryable
import com.quietchatter.talk.domain.ForbiddenException
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@WebMvcTest(TalkController::class, ReactionController::class)
class GlobalExceptionHandlerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var talkCommandable: TalkCommandable

    @MockitoBean
    private lateinit var talkQueryable: TalkQueryable

    @MockitoBean
    private lateinit var reactionModifiable: ReactionModifiable

    @Test
    fun `missing X-Member-Id header on auth-required endpoint returns 401`() {
        mockMvc.perform(
            post("/api/talks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"bookId":"${UUID.randomUUID()}","content":"test"}""")
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `ForbiddenException from service returns 403`() {
        whenever(talkQueryable.getHiddenTalksByMember(any(), any(), any()))
            .thenThrow(ForbiddenException("접근 권한이 없습니다."))

        mockMvc.perform(
            get("/api/talks")
                .param("memberId", UUID.randomUUID().toString())
                .param("hidden", "true")
                .header("X-Member-Id", UUID.randomUUID().toString())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isForbidden)
    }
}
