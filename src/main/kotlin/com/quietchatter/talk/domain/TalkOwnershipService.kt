package com.quietchatter.talk.domain

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TalkOwnershipService {

    fun hideByOwner(talk: Talk, requesterId: UUID) {
        verifyOwner(talk.memberId, requesterId)
        talk.hide()
    }

    fun hideByAdmin(talk: Talk) {
        talk.hide()
    }

    fun hideBySystem(talk: Talk) {
        talk.hide()
    }

    fun restoreByOwner(talk: Talk, requesterId: UUID) {
        verifyOwner(talk.memberId, requesterId)
        talk.restore()
    }

    fun updateContentByOwner(talk: Talk, requesterId: UUID, content: String) {
        verifyOwner(talk.memberId, requesterId)
        talk.updateContent(content)
    }

    fun authorizeHiddenAccess(memberId: UUID, requesterId: UUID) {
        verifyOwner(memberId, requesterId)
    }

    private fun verifyOwner(ownerId: UUID, requesterId: UUID) {
        if (ownerId != requesterId) throw ForbiddenException("접근 권한이 없습니다.")
    }
}
