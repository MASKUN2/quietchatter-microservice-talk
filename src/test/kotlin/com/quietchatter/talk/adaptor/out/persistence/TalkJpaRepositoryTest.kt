package com.quietchatter.talk.adaptor.out.persistence

import com.quietchatter.talk.domain.Talk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.util.*

@DataJpaTest
@ActiveProfiles("test")
class TalkJpaRepositoryTest {

    @Autowired
    private lateinit var talkJpaRepository: TalkJpaRepository

    @Test
    fun `findByBookId should only return non-hidden talks`() {
        // Given
        val bookId = UUID.randomUUID()
        val memberId = UUID.randomUUID()
        
        val visibleTalk = Talk(
            bookId = bookId,
            memberId = memberId,
            nickname = "tester",
            content = "visible content"
        )
        
        val hiddenTalk = Talk(
            bookId = bookId,
            memberId = memberId,
            nickname = "tester",
            content = "hidden content"
        ).apply { hide() }
        
        talkJpaRepository.saveAll(listOf(visibleTalk, hiddenTalk))

        // When
        val result = talkJpaRepository.findByBookId(bookId, PageRequest.of(0, 10))

        // Then
        assertEquals(1, result.totalElements)
        assertEquals("visible content", result.content[0].content)
    }

    @Test
    fun `findByMemberId should only return non-hidden talks`() {
        // Given
        val bookId = UUID.randomUUID()
        val memberId = UUID.randomUUID()
        
        val visibleTalk = Talk(
            bookId = bookId,
            memberId = memberId,
            nickname = "tester",
            content = "visible content"
        )
        
        val hiddenTalk = Talk(
            bookId = bookId,
            memberId = memberId,
            nickname = "tester",
            content = "hidden content"
        ).apply { hide() }
        
        talkJpaRepository.saveAll(listOf(visibleTalk, hiddenTalk))

        // When
        val result = talkJpaRepository.findByMemberId(memberId, PageRequest.of(0, 10))

        // Then
        assertEquals(1, result.totalElements)
    }
}
