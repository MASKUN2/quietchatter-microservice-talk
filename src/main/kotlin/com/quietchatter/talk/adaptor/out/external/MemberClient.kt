package com.quietchatter.talk.adaptor.out.external

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import java.util.UUID

@FeignClient(name = "member-service", url = "\${MEMBER_SERVICE_URL:http://microservice-member:8083}")
interface MemberClient {
    @GetMapping("/internal/api/members/{memberId}")
    fun getMemberInfo(
        @PathVariable("memberId") memberId: UUID,
        @RequestHeader("X-Internal-Secret") secret: String = "default-internal-secret"
    ): InternalMemberResponse
}

data class InternalMemberResponse(val id: UUID, val nickname: String)
