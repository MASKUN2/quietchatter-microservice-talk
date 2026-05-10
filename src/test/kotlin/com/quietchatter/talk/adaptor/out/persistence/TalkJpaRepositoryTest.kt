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
        val memberId = UUID.randomUUID()
        talkJpaRepository.saveAll(listOf(
            Talk(UUID.randomUUID(), memberId, "tester", "visible content"),
            Talk(UUID.randomUUID(), memberId, "tester", "hidden content").apply { hide() }
        ))

        val result = talkJpaRepository.findByMemberId(memberId, PageRequest.of(0, 10))

        assertEquals(1, result.totalElements)
        assertEquals("visible content", result.content[0].content)
    }

    @Test
    fun `findByMemberIdAndIsHidden with true should return only hidden talks`() {
        val memberId = UUID.randomUUID()
        talkJpaRepository.saveAll(listOf(
            Talk(UUID.randomUUID(), memberId, "tester", "visible content"),
            Talk(UUID.randomUUID(), memberId, "tester", "hidden content").apply { hide() }
        ))

        val result = talkJpaRepository.findByMemberIdAndIsHidden(memberId, true, PageRequest.of(0, 10))

        assertEquals(1, result.totalElements)
        assertEquals("hidden content", result.content[0].content)
    }

    @Test
    fun `findByMemberIdAndIsHidden with false should return only visible talks`() {
        val memberId = UUID.randomUUID()
        talkJpaRepository.saveAll(listOf(
            Talk(UUID.randomUUID(), memberId, "tester", "visible content"),
            Talk(UUID.randomUUID(), memberId, "tester", "hidden content").apply { hide() }
        ))

        val result = talkJpaRepository.findByMemberIdAndIsHidden(memberId, false, PageRequest.of(0, 10))

        assertEquals(1, result.totalElements)
        assertEquals("visible content", result.content[0].content)
    }

    @Test
    fun `findByMemberIdAndIsHidden should not return other members talks`() {
        val memberId = UUID.randomUUID()
        val otherMemberId = UUID.randomUUID()
        talkJpaRepository.saveAll(listOf(
            Talk(UUID.randomUUID(), memberId, "tester", "my hidden").apply { hide() },
            Talk(UUID.randomUUID(), otherMemberId, "other", "other hidden").apply { hide() }
        ))

        val result = talkJpaRepository.findByMemberIdAndIsHidden(memberId, true, PageRequest.of(0, 10))

        assertEquals(1, result.totalElements)
        assertEquals("my hidden", result.content[0].content)
    }
}
