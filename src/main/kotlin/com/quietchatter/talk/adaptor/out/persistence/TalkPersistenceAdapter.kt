package com.quietchatter.talk.adaptor.out.persistence

import com.quietchatter.talk.application.out.TalkLoadable
import com.quietchatter.talk.application.out.TalkPersistable
import com.quietchatter.talk.domain.Talk
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.UUID

@Component
class TalkPersistenceAdapter(
    private val talkJpaRepository: TalkJpaRepository
) : TalkPersistable, TalkLoadable {

    override fun save(talk: Talk): Talk {
        return talkJpaRepository.save(talk)
    }

    override fun delete(talk: Talk) {
        talkJpaRepository.delete(talk)
    }

    override fun hideAllByMemberId(memberId: UUID) {
        talkJpaRepository.hideAllByMemberId(memberId)
    }

    override fun hideExpiredTalks(now: LocalDate): Int {
        return talkJpaRepository.hideExpiredTalks(now)
    }

    override fun findExpiredTalks(now: LocalDate): List<Talk> {
        return talkJpaRepository.findExpiredTalks(now)
    }

    override fun updateNicknameByMemberId(memberId: UUID, nickname: String) {
        talkJpaRepository.updateNicknameByMemberId(memberId, nickname)
    }

    override fun findById(id: UUID): Talk? {
        return talkJpaRepository.findById(id).orElse(null)
    }

    override fun findByBookId(bookId: UUID, pageable: Pageable): Page<Talk> {
        return talkJpaRepository.findByBookId(bookId, pageable)
    }

    override fun findByMemberId(memberId: UUID, pageable: Pageable): Page<Talk> {
        return talkJpaRepository.findByMemberId(memberId, pageable)
    }

    @Cacheable(cacheNames = ["recommendedTalks"], key = "#size")
    override fun findRecommended(size: Int): List<Talk> {
        return talkJpaRepository.findRecommended(PageRequest.of(0, size))
    }
}
