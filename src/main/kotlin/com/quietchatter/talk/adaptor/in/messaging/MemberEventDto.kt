package com.quietchatter.talk.adaptor.`in`.messaging

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class MemberEventDto(
    @JsonProperty("evt_type") val evtType: String,
    @JsonProperty("memberId") val memberId: String? = null,
    @JsonProperty("nickname") val nickname: String? = null
)
