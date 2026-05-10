package com.quietchatter.talk.application.`in`

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime
import java.util.UUID

interface TalkQueryable {
    fun getTalksByBook(bookId: UUID, memberId: UUID?, pageable: Pageable): Page<TalkDetail>
    fun getRecommendedTalks(size: Int, memberId: UUID?): List<TalkDetail>
    fun getVisibleTalksByMember(memberId: UUID, pageable: Pageable): Page<TalkDetail>
    fun getHiddenTalksByMember(memberId: UUID, requesterId: UUID, pageable: Pageable): Page<TalkDetail>
}

data class TalkDetail(
    val id: UUID,
    val bookId: UUID,
    val memberId: UUID,
    val nickname: String,
    val content: String,
    val likeCount: Long,
    val supportCount: Long,
    val didILike: Boolean,
    val didISupport: Boolean,
    val createdAt: LocalDateTime,
    val isModified: Boolean
)
