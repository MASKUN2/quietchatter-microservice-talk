package com.quietchatter.talk.adaptor.`in`.web

import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.epages.restdocs.apispec.Schema
import com.quietchatter.talk.application.`in`.*
import com.quietchatter.talk.domain.ReactionType
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime
import java.util.*

@WebMvcTest(TalkController::class, ReactionController::class)
@AutoConfigureRestDocs
@Tag("restdocs")
class TalkControllerDocTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var talkQueryable: TalkQueryable

    @MockitoBean
    private lateinit var talkCommandable: TalkCommandable

    @MockitoBean
    private lateinit var reactionModifiable: ReactionModifiable

    @Test
    fun getTalksByMember() {
        val memberId = UUID.randomUUID()
        val talkDetail = TalkDetail(
            id = UUID.randomUUID(),
            bookId = UUID.randomUUID(),
            memberId = memberId,
            nickname = "tester",
            content = "test content",
            likeCount = 10,
            supportCount = 5,
            didILike = false,
            didISupport = false,
            createdAt = LocalDateTime.now(),
            isModified = false
        )

        whenever(talkQueryable.getTalksByMember(any(), any())).thenReturn(PageImpl(listOf(talkDetail)))

        mockMvc.perform(
            get("/api/talks")
                .param("memberId", memberId.toString())
                .header("X-Member-Id", memberId.toString())
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "get-talks-by-member",
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag("Talks")
                            .description("Get talks by member ID")
                            .queryParameters(
                                parameterWithName("memberId").description("The unique identifier of the member")
                            )
                            .responseFields(
                                fieldWithPath("content[].id").description("Talk ID"),
                                fieldWithPath("content[].bookId").description("Book ID"),
                                fieldWithPath("content[].memberId").description("Member ID"),
                                fieldWithPath("content[].nickname").description("Nickname"),
                                fieldWithPath("content[].content").description("Content"),
                                fieldWithPath("content[].likeCount").description("Like Count"),
                                fieldWithPath("content[].supportCount").description("Support Count"),
                                fieldWithPath("content[].didILike").description("Did I Like"),
                                fieldWithPath("content[].didISupport").description("Did I Support"),
                                fieldWithPath("content[].createdAt").description("Created At"),
                                fieldWithPath("content[].isModified").description("Is Modified"),
                                fieldWithPath("page.size").description("Page size"),
                                fieldWithPath("page.number").description("Current page number"),
                                fieldWithPath("page.totalElements").description("Total elements"),
                                fieldWithPath("page.totalPages").description("Total pages")
                            )
                            .responseSchema(Schema.schema("TalkPageResponse"))
                            .build()
                    )
                )
            )
    }

    @Test
    fun getTalksByMemberForbidden() {
        val memberId = UUID.randomUUID()
        val otherMemberId = UUID.randomUUID()

        mockMvc.perform(
            get("/api/talks")
                .param("memberId", memberId.toString())
                .header("X-Member-Id", otherMemberId.toString())
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isForbidden)
            .andDo(
                document(
                    "get-talks-by-member-forbidden",
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag("Talks")
                            .description("Get talks by member ID - Forbidden when memberId does not match X-Member-Id header")
                            .queryParameters(
                                parameterWithName("memberId").description("The unique identifier of the member")
                            )
                            .build()
                    )
                )
            )
    }

    @Test
    fun getTalksByBook() {
        val bookId = UUID.randomUUID()
        val talkDetail = TalkDetail(
            id = UUID.randomUUID(),
            bookId = bookId,
            memberId = UUID.randomUUID(),
            nickname = "tester",
            content = "test content",
            likeCount = 10,
            supportCount = 5,
            didILike = false,
            didISupport = false,
            createdAt = LocalDateTime.now(),
            isModified = false
        )

        whenever(talkQueryable.getTalksByBook(any(), any(), any())).thenReturn(PageImpl(listOf(talkDetail)))

        mockMvc.perform(
            get("/api/talks/book/{bookId}", bookId)
                .header("X-Member-Id", UUID.randomUUID().toString())
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "get-talks-by-book",
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag("Talks")
                            .description("Get talks by book ID")
                            .pathParameters(
                                parameterWithName("bookId").description("The unique identifier of the book")
                            )
                            .responseFields(
                                fieldWithPath("content[].id").description("Talk ID"),
                                fieldWithPath("content[].bookId").description("Book ID"),
                                fieldWithPath("content[].memberId").description("Member ID"),
                                fieldWithPath("content[].nickname").description("Nickname"),
                                fieldWithPath("content[].content").description("Content"),
                                fieldWithPath("content[].likeCount").description("Like Count"),
                                fieldWithPath("content[].supportCount").description("Support Count"),
                                fieldWithPath("content[].didILike").description("Did I Like"),
                                fieldWithPath("content[].didISupport").description("Did I Support"),
                                fieldWithPath("content[].createdAt").description("Created At"),
                                fieldWithPath("content[].isModified").description("Is Modified"),
                                fieldWithPath("page.size").description("Page size"),
                                fieldWithPath("page.number").description("Current page number"),
                                fieldWithPath("page.totalElements").description("Total elements"),
                                fieldWithPath("page.totalPages").description("Total pages")
                            )
                            .responseSchema(Schema.schema("TalkPageResponse"))
                            .build()
                    )
                )
            )
    }

    @Test
    fun createTalk() {
        val talkId = UUID.randomUUID()
        val request = """
            {
                "bookId": "${UUID.randomUUID()}",
                "content": "new talk content",
                "dateToHidden": "2027-05-01"
            }
        """.trimIndent()

        whenever(talkCommandable.createTalk(any())).thenReturn(talkId)

        mockMvc.perform(
            post("/api/talks")
                .header("X-Member-Id", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        )
            .andExpect(status().isCreated)
            .andDo(
                document(
                    "create-talk",
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag("Talks")
                            .description("Create a new talk")
                            .requestFields(
                                fieldWithPath("bookId").description("Book ID"),
                                fieldWithPath("content").description("Talk content"),
                                fieldWithPath("dateToHidden").description("Date when talk becomes hidden (optional, defaults to 1 year)").optional()
                            )
                            .build()
                    )
                )
            )
    }

    @Test
    fun deleteTalk() {
        val talkId = UUID.randomUUID()

        mockMvc.perform(
            delete("/api/talks/{talkId}", talkId)
                .header("X-Member-Id", UUID.randomUUID().toString())
        )
            .andExpect(status().isNoContent)
            .andDo(
                document(
                    "delete-talk",
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag("Talks")
                            .description("Delete (soft-delete) an existing talk")
                            .pathParameters(
                                parameterWithName("talkId").description("Talk ID")
                            )
                            .build()
                    )
                )
            )
    }

    @Test
    fun getRecommendedTalks() {
        val talkDetail = TalkDetail(
            id = UUID.randomUUID(),
            bookId = UUID.randomUUID(),
            memberId = UUID.randomUUID(),
            nickname = "tester",
            content = "test content",
            likeCount = 10,
            supportCount = 5,
            didILike = false,
            didISupport = false,
            createdAt = LocalDateTime.now(),
            isModified = false
        )

        whenever(talkQueryable.getRecommendedTalks(any(), any())).thenReturn(listOf(talkDetail))

        mockMvc.perform(
            get("/api/talks/recommended")
                .header("X-Member-Id", UUID.randomUUID().toString())
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andDo(
                document(
                    "get-recommended-talks",
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag("Talks")
                            .description("Get recommended talks")
                            .queryParameters(
                                parameterWithName("size").description("Number of talks to return (default: 5)").optional()
                            )
                            .responseFields(
                                fieldWithPath("[].id").description("Talk ID"),
                                fieldWithPath("[].bookId").description("Book ID"),
                                fieldWithPath("[].memberId").description("Member ID"),
                                fieldWithPath("[].nickname").description("Nickname"),
                                fieldWithPath("[].content").description("Content"),
                                fieldWithPath("[].likeCount").description("Like Count"),
                                fieldWithPath("[].supportCount").description("Support Count"),
                                fieldWithPath("[].didILike").description("Did I Like"),
                                fieldWithPath("[].didISupport").description("Did I Support"),
                                fieldWithPath("[].createdAt").description("Created At"),
                                fieldWithPath("[].isModified").description("Is Modified")
                            )
                            .responseSchema(Schema.schema("TalkListResponse"))
                            .build()
                    )
                )
            )
    }

    @Test
    fun removeReaction() {
        val talkId = UUID.randomUUID()
        val request = """
            {
                "type": "LIKE"
            }
        """.trimIndent()

        mockMvc.perform(
            delete("/api/reactions/talks/{talkId}", talkId)
                .header("X-Member-Id", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        )
            .andExpect(status().isNoContent)
            .andDo(
                document(
                    "remove-reaction",
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag("Reactions")
                            .description("Remove a reaction from a talk")
                            .pathParameters(
                                parameterWithName("talkId").description("Talk ID")
                            )
                            .requestFields(
                                fieldWithPath("type").description("Reaction type (LIKE, SUPPORT)")
                            )
                            .build()
                    )
                )
            )
    }

    @Test
    fun updateTalk() {
        val talkId = UUID.randomUUID()
        val request = """
            {
                "content": "updated talk content"
            }
        """.trimIndent()

        mockMvc.perform(
            put("/api/talks/{talkId}", talkId)
                .header("X-Member-Id", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        )
            .andExpect(status().isNoContent)
            .andDo(
                document(
                    "update-talk",
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag("Talks")
                            .description("Update an existing talk")
                            .pathParameters(
                                parameterWithName("talkId").description("Talk ID")
                            )
                            .requestFields(
                                fieldWithPath("content").description("Updated talk content")
                            )
                            .build()
                    )
                )
            )
    }

    @Test
    fun addReaction() {
        val talkId = UUID.randomUUID()
        val request = """
            {
                "type": "LIKE"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/reactions/talks/{talkId}", talkId)
                .header("X-Member-Id", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        )
            .andExpect(status().isNoContent)
            .andDo(
                document(
                    "add-reaction",
                    resource(
                        ResourceSnippetParameters.builder()
                            .tag("Reactions")
                            .description("Add a reaction to a talk")
                            .pathParameters(
                                parameterWithName("talkId").description("Talk ID")
                            )
                            .requestFields(
                                fieldWithPath("type").description("Reaction type (LIKE, SUPPORT)")
                            )
                            .build()
                    )
                )
            )
    }
}
