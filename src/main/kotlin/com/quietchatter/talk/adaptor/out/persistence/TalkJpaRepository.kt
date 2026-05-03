package com.quietchatter.talk.adaptor.out.persistence

import com.quietchatter.talk.domain.Talk
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.util.UUID

interface TalkJpaRepository : JpaRepository<Talk, UUID> {
    @Query("SELECT t FROM Talk t WHERE t.bookId = :bookId AND t.isHidden = false")
    fun findByBookId(bookId: UUID, pageable: Pageable): Page<Talk>

    @Query("SELECT t FROM Talk t WHERE t.memberId = :memberId AND t.isHidden = false")
    fun findByMemberId(memberId: UUID, pageable: Pageable): Page<Talk>

    @Query(value = "SELECT * FROM talk WHERE is_hidden = false ORDER BY RANDOM()", nativeQuery = true)
    fun findRecommended(pageable: Pageable): List<Talk>

    @Modifying
    @Query("UPDATE Talk t SET t.isHidden = true WHERE t.memberId = :memberId AND t.isHidden = false")
    fun hideAllByMemberId(memberId: UUID)

    @Modifying
    @Query("UPDATE Talk t SET t.isHidden = true WHERE t.dateToHidden <= :now AND t.isHidden = false")
    fun hideExpiredTalks(now: LocalDate): Int

    @Query("SELECT t FROM Talk t WHERE t.dateToHidden <= :now AND t.isHidden = false")
    fun findExpiredTalks(now: LocalDate): List<Talk>

    @Modifying
    @Query("UPDATE Talk t SET t.nickname = :nickname WHERE t.memberId = :memberId")
    fun updateNicknameByMemberId(memberId: UUID, nickname: String)
}
