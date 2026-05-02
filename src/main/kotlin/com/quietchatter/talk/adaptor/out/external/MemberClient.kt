package com.quietchatter.talk.adaptor.out.external

import feign.RequestInterceptor
import feign.RequestTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.util.UUID

class MemberClientConfig(
    @Value("\${INTERNAL_SECRET:default-internal-secret}") val internalSecret: String
) : RequestInterceptor {
    override fun apply(template: RequestTemplate) {
        template.header("X-Internal-Secret", internalSecret)
    }
}

@FeignClient(
    name = "member-service",
    url = "\${MEMBER_SERVICE_URL:http://member.quietchatter.svc.cluster.local:8083}",
    configuration = [MemberClientConfig::class]
)
interface MemberClient {
    @GetMapping("/internal/api/members/{memberId}")
    fun getMemberInfo(@PathVariable("memberId") memberId: UUID): InternalMemberResponse
}

data class InternalMemberResponse(val id: UUID, val nickname: String)
