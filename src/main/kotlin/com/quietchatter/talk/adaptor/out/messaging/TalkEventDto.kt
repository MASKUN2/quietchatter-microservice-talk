package com.quietchatter.talk.adaptor.out.messaging

import com.fasterxml.jackson.annotation.JsonProperty

data class TalkEventDto(
    @JsonProperty("eventId") val eventId: String,
    @JsonProperty("aggregateId") val aggregateId: String,
    @JsonProperty("evt_type") val evtType: String,
    @JsonProperty("payload") val payload: String,
    @JsonProperty("occurredAt") val occurredAt: String
)
