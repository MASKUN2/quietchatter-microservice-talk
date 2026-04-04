package com.quietchatter.talk.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.util.UUID

@Entity
@Table(
    name = "reaction",
    indexes = [
        Index(name = "idx_reaction_talk_id", columnList = "talk_id"),
        Index(name = "idx_reaction_member_id", columnList = "member_id"),
        Index(name = "idx_reaction_type", columnList = "type")
    ],
    uniqueConstraints = [
        UniqueConstraint(name = "uq_reaction_talk_member_type", columnNames = ["talk_id", "member_id", "type"])
    ]
)
class Reaction(
    @Column(name = "talk_id", nullable = false)
    val talkId: UUID,

    @Column(name = "member_id", nullable = false)
    val memberId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: ReactionType
) : BaseEntity()
