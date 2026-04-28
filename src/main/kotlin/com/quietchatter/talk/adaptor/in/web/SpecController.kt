package com.quietchatter.talk.adaptor.`in`.web

import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.nio.charset.StandardCharsets

@RestController
class SpecController {

    @GetMapping("/api/spec")
    fun getSpec(): ResponseEntity<String> {
        return try {
            // JAR 내부 정적 리소스 경로 확인
            val resource = ClassPathResource("static/docs/openapi3.yaml")
            if (resource.exists()) {
                val yaml = resource.inputStream.bufferedReader(StandardCharsets.UTF_8).readText()
                ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/x-yaml"))
                    .body(yaml)
            } else {
                // 로컬 개발 환경용 폴백 (build 디렉토리 확인)
                val localResource = ClassPathResource("static/docs/openapi3.yaml") // BootJar 설정에 의해 빌드 시 복사됨
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            ResponseEntity.internalServerError().build()
        }
    }
}
