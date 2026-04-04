package com.quietchatter.talk.application.out

import com.quietchatter.talk.domain.Talk
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface TalkLoadable {
    fun findById(id: UUID): Talk?
    fun findByBookId(bookId: UUID, pageable: Pageable): Page<Talk>
    fun findByMemberId(memberId: UUID, pageable: Pageable): Page<Talk>
    fun findRecommended(size: Int): List<Talk>
}
