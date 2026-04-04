package com.quietchatter.talk.application.out

import com.quietchatter.talk.domain.Talk
import java.time.LocalDate
import java.util.UUID

interface TalkPersistable {
    fun save(talk: Talk): Talk
    fun delete(talk: Talk)
    fun hideAllByMemberId(memberId: UUID)
    fun hideExpiredTalks(now: LocalDate): Int
}
