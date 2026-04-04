package com.quietchatter.talk.application.`out`

import com.quietchatter.talk.domain.Reaction
import com.quietchatter.talk.domain.ReactionType
import java.util.UUID

interface ReactionPersistable {
    fun save(reaction: Reaction): Reaction
    fun delete(reaction: Reaction)
}
