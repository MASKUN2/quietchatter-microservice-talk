package com.quietchatter.talk.application

import com.quietchatter.talk.application.`in`.AddReactionCommand
import com.quietchatter.talk.application.`in`.ReactionModifiable
import com.quietchatter.talk.application.`in`.RemoveReactionCommand
import com.quietchatter.talk.application.out.ReactionLoadable
import com.quietchatter.talk.application.out.ReactionPersistable
import com.quietchatter.talk.application.out.TalkLoadable
import com.quietchatter.talk.application.out.TalkPersistable
import com.quietchatter.talk.domain.Reaction
import com.quietchatter.talk.domain.ReactionType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ReactionService(
    private val talkLoadable: TalkLoadable,
    private val talkPersistable: TalkPersistable,
    private val reactionPersistable: ReactionPersistable,
    private val reactionLoadable: ReactionLoadable
) : ReactionModifiable {

    override fun addReaction(command: AddReactionCommand) {
        val talk = talkLoadable.findById(command.talkId) ?: throw IllegalArgumentException("Talk not found")
        
        val existingReaction = reactionLoadable.findByTalkIdAndMemberIdAndType(
            command.talkId, command.memberId, command.type
        )
        if (existingReaction != null) return

        val reaction = Reaction(
            talkId = command.talkId,
            memberId = command.memberId,
            type = command.type
        )
        reactionPersistable.save(reaction)
        
        when (command.type) {
            ReactionType.LIKE -> talk.increaseLikeCount()
            ReactionType.SUPPORT -> talk.increaseSupportCount()
        }
        talkPersistable.save(talk)
    }

    override fun removeReaction(command: RemoveReactionCommand) {
        val talk = talkLoadable.findById(command.talkId) ?: throw IllegalArgumentException("Talk not found")
        
        val reaction = reactionLoadable.findByTalkIdAndMemberIdAndType(
            command.talkId, command.memberId, command.type
        ) ?: return

        reactionPersistable.delete(reaction)
        
        when (command.type) {
            ReactionType.LIKE -> talk.decreaseLikeCount()
            ReactionType.SUPPORT -> talk.decreaseSupportCount()
        }
        talkPersistable.save(talk)
    }
}
