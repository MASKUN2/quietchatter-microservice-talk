package com.quietchatter.talk.adaptor.`in`.messaging

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class MemberEventDto(
    @JsonProperty("type") val type: String,
    @JsonProperty("subject") val subject: String? = null,
    @JsonProperty("data") val data: Map<String, Any?>? = null
)
