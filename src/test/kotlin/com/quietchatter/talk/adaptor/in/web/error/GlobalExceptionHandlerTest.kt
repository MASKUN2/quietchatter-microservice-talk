package com.quietchatter.talk.adaptor.`in`.web.error

import com.quietchatter.talk.adaptor.`in`.web.ReactionController
import com.quietchatter.talk.adaptor.`in`.web.TalkController
import com.quietchatter.talk.application.`in`.ReactionModifiable
import com.quietchatter.talk.application.`in`.TalkCommandable
import com.quietchatter.talk.application.`in`.TalkQueryable
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
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
}
