package com.quietchatter.talk.adaptor.`in`.web

import com.quietchatter.talk.application.`in`.*
import com.quietchatter.talk.domain.ForbiddenException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/talks")
class TalkController(
    private val talkCommandable: TalkCommandable,
    private val talkQueryable: TalkQueryable
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
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
    @ResponseStatus(HttpStatus.NO_CONTENT)
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
        @RequestHeader("X-Member-Id", required = false) requesterId: UUID?,
        @RequestParam memberId: UUID,
        @RequestParam(defaultValue = "false") hidden: Boolean,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<Page<TalkDetail>> {
        if (!hidden) return ResponseEntity.ok(talkQueryable.getVisibleTalksByMember(memberId, pageable))
        if (requesterId == null) throw ForbiddenException("접근 권한이 없습니다.")
        return ResponseEntity.ok(talkQueryable.getHiddenTalksByMember(memberId, requesterId, pageable))
    }

    @PostMapping("/{talkId}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun restoreTalk(
        @RequestHeader("X-Member-Id") memberId: UUID,
        @PathVariable talkId: UUID
    ) {
        talkCommandable.restoreTalk(
            RestoreTalkCommand(
                talkId = talkId,
                memberId = memberId
            )
        )
    }

    @GetMapping("/book/{bookId}")
    fun getTalksByBook(
        @RequestHeader("X-Member-Id", required = false) memberId: UUID?,
        @PathVariable bookId: UUID,
        @PageableDefault(size = 20) pageable: Pageable
    ): Page<TalkDetail> {
        return talkQueryable.getTalksByBook(bookId, memberId, pageable)
    }

    @GetMapping("/recommended")
    fun getRecommendedTalks(
        @RequestHeader("X-Member-Id", required = false) memberId: UUID?,
        @RequestParam(defaultValue = "5") size: Int
    ): List<TalkDetail> {
        return talkQueryable.getRecommendedTalks(size, memberId)
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

