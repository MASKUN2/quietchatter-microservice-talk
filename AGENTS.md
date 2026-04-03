# AI Agent Guide - microservice-talk

이 문서는 AI 에이전트가 microservice-talk 프로젝트를 이해하고 개발을 돕기 위한 지침입니다.

## 1. 서비스 개요

* 역할: 북톡(BookTalk) CRUD 및 Reaction(좋아요/반응) 관리
* 담당 레거시 패키지: `talk`, `reaction`
* 포트: 8083

## 2. 기술 스택

* 언어: Kotlin 1.9.x
* 프레임워크: Spring Boot 3.5.13
* 데이터베이스: PostgreSQL (JPA)
* 의존성: spring-boot-starter-web, data-jpa, consul-discovery, consul-config

## 3. 아키텍처

헥사고날 아키텍처(Ports and Adapters)를 사용합니다.

```
adaptor/in  (Web Controller: @RestController)
    |
application (Use Case Service: Port Interface + Impl)
    |
adaptor/out (JPA Repository, External Service Client)
    |
domain      (Talk, Reaction Entity: 순수 비즈니스 로직)
```

패키지 구조 예시:
```
com.quietchatter.talk/
  domain/          Talk.kt, Reaction.kt, ReactionType.kt
  application/
    in/            TalkCommandable.kt, TalkQueryable.kt, ReactionModifiable.kt
    out/           TalkRepository.kt, ReactionRepository.kt
  adaptor/
    in/            TalkCommandController.kt, TalkQueryController.kt
                   RecommendTalkController.kt, ReactionCommandController.kt
                   InternalTalkController.kt
    out/           TalkJpaRepository.kt, ReactionJpaRepository.kt
```

## 4. 에이전트 작업 지침

### A. 코드 포팅 규칙

* 레거시 Java 코드를 idiomatic Kotlin 코드로 변환하십시오.
* Data class를 적극 활용하십시오. Lombok은 사용하지 않습니다.
* 레거시의 `talk/`, `reaction/` 패키지 전체를 참고하여 포팅하십시오.
* 새로운 코드를 작성하거나 수정할 때마다 반드시 단위 테스트(Unit Test)를 함께 작성하고 통과를 확인하십시오.

### B. 사용자 인증 처리

* 인증이 필요한 API는 `X-Member-Id` 헤더를 파라미터로 받아 사용하십시오.
* 인증 선택적 API(예: 북톡 목록 조회)는 `X-Member-Id` 헤더가 없어도 동작해야 합니다.
* 헤더가 없으면 비로그인 상태로 처리합니다 (예: 반응 표시 없음).

### C. 숨김(soft delete) 처리

* 북톡 삭제는 물리 삭제가 아닌 숨김(hidden) 처리입니다.
* 레거시의 `Talk.java`에서 `hidden` 필드와 처리 방식을 참고하십시오.
* 목록 조회 시 숨겨진 북톡은 제외하십시오.

### D. 서비스 간 통신

* 북톡 목록 응답에 책 정보(제목, 썸네일)가 필요하면 microservice-book의 내부 API를 호출하십시오.
* 통신 방식: Spring RestClient + Consul LoadBalancer

### E. 문서 규칙

* 마크다운 작성 시 굵게(bold)나 기울임(italics) 같은 강조 서식을 사용하지 않습니다.
* 마크다운 작성 시 이모티콘을 사용하지 않습니다.

## 5. 구현 스펙 참조

[docs/spec.md](./docs/spec.md)를 반드시 읽고 작업을 시작하십시오.
