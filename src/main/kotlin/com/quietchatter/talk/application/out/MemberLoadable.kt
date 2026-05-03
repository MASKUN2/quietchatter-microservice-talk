package com.quietchatter.talk.application.out

import java.util.UUID

interface MemberLoadable {
    fun getMemberNickname(memberId: UUID): String
}
