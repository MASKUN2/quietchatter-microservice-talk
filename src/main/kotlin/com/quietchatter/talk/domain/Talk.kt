package com.quietchatter.talk.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(
    name = "talk",
    indexes = [
        Index(name = "idx_talk_book_id", columnList = "book_id"),
        Index(name = "idx_talk_member_id", columnList = "member_id"),
        Index(name = "idx_talk_created_at", columnList = "created_at"),
        Index(name = "idx_talk_date_to_hidden_is_hidden", columnList = "date_to_hidden, is_hidden")
    ]
)
class Talk(
    @Column(name = "book_id", nullable = false)
    val bookId: UUID,

    @Column(name = "member_id", nullable = false)
    val memberId: UUID,

    @Column(name = "nickname", nullable = false)
    val nickname: String,

    @Column(name = "content", length = 250, nullable = false)
    var content: String,

    @Column(name = "date_to_hidden")
    var dateToHidden: LocalDate? = null
) : BaseEntity() {

    @Column(name = "is_hidden", nullable = false)
    var isHidden: Boolean = false
        private set

    @Column(name = "like_count", nullable = false)
    var likeCount: Long = 0
        private set

    @Column(name = "support_count", nullable = false)
    var supportCount: Long = 0
        private set

    init {
        validateContent(content)
        if (dateToHidden == null) {
            this.dateToHidden = LocalDate.now().plusMonths(12)
        }
    }

    private fun validateContent(content: String) {
        require(content.isNotBlank()) { "내용은 비어있을 수 없습니다." }
        require(content.length <= 250) { "내용은 250자를 초과할 수 없습니다." }
    }

    fun updateContent(newContent: String) {
        validateContent(newContent)
        this.content = newContent
        this.dateToHidden = LocalDate.now().plusMonths(12)
    }

    fun hide() {
        this.isHidden = true
    }

    /**
     * 특정 날짜 기준으로 숨김 처리되어야 하는지 확인합니다.
     */
    fun shouldBeHidden(baseDate: LocalDate = LocalDate.now()): Boolean {
        if (isHidden) return true
        return dateToHidden?.let { it.isBefore(baseDate) || it.isEqual(baseDate) } ?: false
    }

    fun increaseLikeCount() {
        this.likeCount++
    }

    fun decreaseLikeCount() {
        if (this.likeCount > 0) this.likeCount--
    }

    fun increaseSupportCount() {
        this.supportCount++
    }

    fun decreaseSupportCount() {
        if (this.supportCount > 0) this.supportCount--
    }

    @JsonIgnore
    fun isModified(): Boolean {
        if (createdAt == null || lastModifiedAt == null) return false
        return java.time.temporal.ChronoUnit.SECONDS.between(createdAt, lastModifiedAt) > 0
    }
}
