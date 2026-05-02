package com.quietchatter.talk.adaptor.`in`.web

import com.quietchatter.talk.application.`in`.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@RestController
@RequestMapping("/api/talks")
class TalkController(
    private val talkCommandable: TalkCommandable,
    private val talkQueryable: TalkQueryable
) {

    @PostMapping
    fun createTalk(
        @RequestHeader("X-Member-Id") memberId: UUID,
        @RequestBody request: CreateTalkRequest
    ): UUID {
        return talkCommandable.createTalk(
            CreateTalkCommand(
                bookId = request.bookId,
                memberId = memberId,
                nickname = "", // 이제 Service에서 조회하므로 빈 문자열 전달 (또는 Command 구조 변경 가능)
                content = request.content,
                dateToHidden = request.dateToHidden
            )
        )
    }

    @PutMapping("/{talkId}")
    fun updateTalk(
        @RequestHeader("X-Member-Id") memberId: UUID,
        @PathVariable talkId: UUID,
        @RequestBody request: UpdateTalkRequest
    ) {
        talkCommandable.updateTalk(
            UpdateTalkCommand(
                talkId = talkId,
                memberId = memberId,
                content = request.content
            )
        )
    }

    @DeleteMapping("/{talkId}")
    fun deleteTalk(
        @RequestHeader("X-Member-Id") memberId: UUID,
        @PathVariable talkId: UUID
    ) {
        talkCommandable.deleteTalk(
            DeleteTalkCommand(
                talkId = talkId,
                memberId = memberId
            )
        )
    }

    @GetMapping
    fun getTalks(
        @RequestHeader("X-Member-Id", required = false) headerMemberId: UUID?,
        @RequestParam(required = false) memberId: UUID?,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<Page<TalkResponse>> {
        if (memberId == null) {
            return ResponseEntity.badRequest().build()
        }

        if (headerMemberId != memberId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val talks = talkQueryable.getTalksByMember(memberId, pageable).map { it.toResponse() }
        return ResponseEntity.ok(talks)
    }

    @GetMapping("/book/{bookId}")
    fun getTalksByBook(
        @RequestHeader("X-Member-Id", required = false) memberId: UUID?,
        @PathVariable bookId: UUID,
        @PageableDefault(size = 20) pageable: Pageable
    ): Page<TalkResponse> {
        return talkQueryable.getTalksByBook(bookId, memberId, pageable).map { it.toResponse() }
    }

    @GetMapping("/recommended")
    fun getRecommendedTalks(
        @RequestHeader("X-Member-Id", required = false) memberId: UUID?,
        @RequestParam(defaultValue = "5") size: Int
    ): List<TalkResponse> {
        return talkQueryable.getRecommendedTalks(size, memberId).map { it.toResponse() }
    }
}

data class CreateTalkRequest(
    val bookId: UUID,
    val content: String,
    val dateToHidden: LocalDate? = null
)

data class UpdateTalkRequest(
    val content: String
)

data class TalkResponse(
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

fun TalkDetail.toResponse() = TalkResponse(
    id = id,
    bookId = bookId,
    memberId = memberId,
    nickname = nickname,
    content = content,
    likeCount = likeCount,
    supportCount = supportCount,
    didILike = didILike,
    didISupport = didISupport,
    createdAt = createdAt,
    isModified = isModified
)
