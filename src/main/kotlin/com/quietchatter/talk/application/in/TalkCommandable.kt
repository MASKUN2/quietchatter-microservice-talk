package com.quietchatter.talk.application.`in`

import java.time.LocalDate
import java.util.UUID

interface TalkCommandable {
    fun createTalk(command: CreateTalkCommand): UUID
    fun updateTalk(command: UpdateTalkCommand)
    fun deleteTalk(command: DeleteTalkCommand)
    fun restoreTalk(command: RestoreTalkCommand)
    fun hideAllByMember(memberId: UUID)
    fun hideExpiredTalks(): Int
    fun updateAuthorNickname(memberId: UUID, newNickname: String)
}

data class CreateTalkCommand(
    val bookId: UUID,
    val memberId: UUID,
    val nickname: String,
    val content: String,
    val dateToHidden: LocalDate? = null
)

data class UpdateTalkCommand(
    val talkId: UUID,
    val memberId: UUID,
    val content: String
)

data class DeleteTalkCommand(
    val talkId: UUID,
    val memberId: UUID
)

data class RestoreTalkCommand(
    val talkId: UUID,
    val memberId: UUID
)
