package com.quietchatter.talk.adaptor.out.persistence

import com.quietchatter.talk.domain.Reaction
import com.quietchatter.talk.domain.ReactionType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime
import java.util.UUID

interface ReactionJpaRepository : JpaRepository<Reaction, UUID> {
    fun findByTalkIdAndMemberIdAndType(talkId: UUID, memberId: UUID, type: ReactionType): Reaction?
    fun existsByTalkIdAndMemberId(talkId: UUID, memberId: UUID): Boolean
    
    @Query("SELECT r.talkId FROM Reaction r WHERE r.talkId IN :talkIds AND r.memberId = :memberId")
    fun findReactedTalkIds(talkIds: List<UUID>, memberId: UUID): List<UUID>

    @Query("SELECT r.type FROM Reaction r WHERE r.talkId = :talkId AND r.memberId = :memberId")
    fun findTypesByTalkIdAndMemberId(talkId: UUID, memberId: UUID): List<ReactionType>

    @Query("SELECT r.type as type, COUNT(r) as count FROM Reaction r WHERE r.createdAt BETWEEN :start AND :end GROUP BY r.type")
    fun countByCreatedAtBetweenGroupByType(start: LocalDateTime, end: LocalDateTime): List<Map<String, Any>>
}
