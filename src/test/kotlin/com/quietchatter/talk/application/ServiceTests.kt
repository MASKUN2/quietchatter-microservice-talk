package com.quietchatter.talk.application

import com.quietchatter.talk.application.`in`.AddReactionCommand
import com.quietchatter.talk.application.`in`.CreateTalkCommand
import com.quietchatter.talk.application.out.ReactionLoadable
import com.quietchatter.talk.application.out.ReactionPersistable
import com.quietchatter.talk.application.out.TalkLoadable
import com.quietchatter.talk.application.out.TalkPersistable
import com.quietchatter.talk.domain.ReactionType
import com.quietchatter.talk.domain.Talk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.util.*

class TalkServiceTest {

    private val talkPersistable: TalkPersistable = mock()
    private val talkLoadable: TalkLoadable = mock()
    private val reactionLoadable: ReactionLoadable = mock()
    private val outboxEventRepository: com.quietchatter.talk.adaptor.out.outbox.OutboxEventRepository = mock()
    private val memberClient: com.quietchatter.talk.adaptor.out.external.MemberClient = mock()
    private val talkService = TalkService(talkPersistable, talkLoadable, reactionLoadable, outboxEventRepository, memberClient)

    @Test
    @DisplayName("새로운 북톡을 성공적으로 생성해야 한다")
    fun createTalk() {
        val memberId = UUID.randomUUID()
        val command = CreateTalkCommand(
            bookId = UUID.randomUUID(),
            memberId = memberId,
            nickname = "tester",
            content = "test content"
        )
        val talkId = UUID.randomUUID()
        
        whenever(memberClient.getMemberInfo(eq(memberId))).thenReturn(
            com.quietchatter.talk.adaptor.out.external.InternalMemberResponse(memberId, "tester")
        )
        
        whenever(talkPersistable.save(any())).thenAnswer { invocation ->
            val talk = invocation.getArgument<Talk>(0)
            ReflectionHelper.setId(talk, talkId)
            talk
        }

        val id = talkService.createTalk(command)

        assertEquals(talkId, id)
        verify(talkPersistable).save(any())
    }
}

class ReactionServiceTest {
    private val talkLoadable: TalkLoadable = mock()
    private val talkPersistable: TalkPersistable = mock()
    private val reactionPersistable: ReactionPersistable = mock()
    private val reactionLoadable: ReactionLoadable = mock()
    private val reactionService = ReactionService(talkLoadable, talkPersistable, reactionPersistable, reactionLoadable)

    @Test
    @DisplayName("좋아요 반응 시 Talk의 likeCount가 증가해야 한다")
    fun addLikeReaction() {
        val talkId = UUID.randomUUID()
        val talk = Talk(UUID.randomUUID(), UUID.randomUUID(), "tester", "content")
        ReflectionHelper.setId(talk, talkId)

        val command = AddReactionCommand(talkId, UUID.randomUUID(), ReactionType.LIKE)

        whenever(talkLoadable.findById(any())).thenReturn(talk)
        whenever(reactionLoadable.findByTalkIdAndMemberIdAndType(any(), any(), any())).thenReturn(null)

        reactionService.addReaction(command)

        assertEquals(1, talk.likeCount)
        assertEquals(0, talk.supportCount)
        verify(reactionPersistable).save(any())
        verify(talkPersistable).save(talk)
    }
}

object ReflectionHelper {
    fun setId(obj: Any, id: UUID) {
        var clazz: Class<*>? = obj.javaClass
        while (clazz != null) {
            try {
                val field = clazz.getDeclaredField("id")
                field.isAccessible = true
                field.set(obj, id)
                return
            } catch (e: NoSuchFieldException) {
                clazz = clazz.superclass
            }
        }
        throw RuntimeException("Could not find field 'id' on ${obj.javaClass}")
    }
}
