package com.quietchatter.talk.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*

class TalkTest {

    @Test
    @DisplayName("Talk 생성 시 기본 숨김 날짜는 12개월 후여야 한다")
    fun createTalkDefaultHiddenDate() {
        val talk = Talk(
            bookId = UUID.randomUUID(),
            memberId = UUID.randomUUID(),
            nickname = "tester",
            content = "hello"
        )

        assertEquals(LocalDate.now().plusMonths(12), talk.dateToHidden)
        assertFalse(talk.isHidden)
    }

    @Test
    @DisplayName("내용 수정 시 숨김 날짜가 12개월 후로 갱신되어야 한다")
    fun updateContentUpdatesHiddenDate() {
        val initialHiddenDate = LocalDate.now().plusDays(10)
        val talk = Talk(
            bookId = UUID.randomUUID(),
            memberId = UUID.randomUUID(),
            nickname = "tester",
            content = "hello",
            dateToHidden = initialHiddenDate
        )

        talk.updateContent("updated content")

        assertEquals("updated content", talk.content)
        assertEquals(LocalDate.now().plusMonths(12), talk.dateToHidden)
    }

    @Test
    @DisplayName("좋아요 및 응원 카운트가 개별적으로 증가/감소해야 한다")
    fun reactionCounts() {
        val talk = Talk(UUID.randomUUID(), UUID.randomUUID(), "tester", "content")

        talk.increaseLikeCount()
        talk.increaseSupportCount()
        talk.increaseSupportCount()

        assertEquals(1, talk.likeCount)
        assertEquals(2, talk.supportCount)

        talk.decreaseLikeCount()
        talk.decreaseSupportCount()

        assertEquals(0, talk.likeCount)
        assertEquals(1, talk.supportCount)
    }

    @Test
    @DisplayName("카운트는 0 미만으로 내려가지 않아야 한다")
    fun reactionCountsNotNegative() {
        val talk = Talk(UUID.randomUUID(), UUID.randomUUID(), "tester", "content")

        talk.decreaseLikeCount()

        assertEquals(0, talk.likeCount)
    }
}
