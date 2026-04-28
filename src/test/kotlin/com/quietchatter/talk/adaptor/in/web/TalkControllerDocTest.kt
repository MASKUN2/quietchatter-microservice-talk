package com.quietchatter.talk.adaptor.`in`.web

import com.epages.restdocs.apispec.ResourceDocumentation.resource
import com.epages.restdocs.apispec.ResourceSnippetParameters
import com.epages.restdocs.apispec.Schema
import com.quietchatter.talk.application.`in`.TalkDetail
import com.quietchatter.talk.application.`in`.TalkQueryable
import com.quietchatter.talk.application.`in`.TalkCommandable
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
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime
import java.util.*

@WebMvcTest(TalkController::class)
@AutoConfigureRestDocs
@Tag("restdocs")
class TalkControllerDocTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var talkQueryable: TalkQueryable

    @MockitoBean
    private lateinit var talkCommandable: TalkCommandable

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
                                fieldWithPath("pageable").description("Pageable info"),
                                fieldWithPath("last").description("Is last page"),
                                fieldWithPath("totalPages").description("Total pages"),
                                fieldWithPath("totalElements").description("Total elements"),
                                fieldWithPath("size").description("Page size"),
                                fieldWithPath("number").description("Current page number"),
                                fieldWithPath("sort.empty").description("Sort empty"),
                                fieldWithPath("sort.sorted").description("Sort sorted"),
                                fieldWithPath("sort.unsorted").description("Sort unsorted"),
                                fieldWithPath("first").description("Is first page"),
                                fieldWithPath("numberOfElements").description("Number of elements in current page"),
                                fieldWithPath("empty").description("Is empty")
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
            get("/api/v1/talks/book/{bookId}", bookId)
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
                                fieldWithPath("pageable").description("Pageable info"),
                                fieldWithPath("last").description("Is last page"),
                                fieldWithPath("totalPages").description("Total pages"),
                                fieldWithPath("totalElements").description("Total elements"),
                                fieldWithPath("size").description("Page size"),
                                fieldWithPath("number").description("Current page number"),
                                fieldWithPath("sort.empty").description("Sort empty"),
                                fieldWithPath("sort.sorted").description("Sort sorted"),
                                fieldWithPath("sort.unsorted").description("Sort unsorted"),
                                fieldWithPath("first").description("Is first page"),
                                fieldWithPath("numberOfElements").description("Number of elements in current page"),
                                fieldWithPath("empty").description("Is empty")
                            )
                            .responseSchema(Schema.schema("TalkPageResponse"))
                            .build()
                    )
                )
            )
    }
}
