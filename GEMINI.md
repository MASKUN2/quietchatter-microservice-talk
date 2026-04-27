# AI Agent Guide - microservice-talk

이 문서는 AI 에이전트가 microservice-talk 프로젝트를 이해하고 개발을 돕기 위한 지침입니다.

## 1. 서비스 개요

- 역할: 북톡(BookTalk) CRUD 및 Reaction(좋아요/반응) 관리
- 담당 레거시 패키지: talk, reaction
- 포트: 8080

## 2. 기술 스택

- 언어: Kotlin 1.9.x
- 프레임워크: Spring Boot 3.5.13
- 데이터베이스: PostgreSQL (JPA)
- 의존성: spring-boot-starter-web, data-jpa, consul-discovery, consul-config

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

모든 작업 시작 전 및 작업 중에 superpowers 스킬 목록을 항상 확인하고 상황에 맞는 스킬을 활성화하여 사용하십시오.

### A. 공통 원칙

- 모든 서비스는 헥사고날 아키텍처를 따르며, 어댑터 패키지 명칭은 adaptor로 통일합니다.

### B. 코드 포팅 규칙

- 레거시 Java 코드를 idiomatic Kotlin 코드로 변환하십시오.
- Data class를 적극 활용하십시오. Lombok은 사용하지 않습니다.
- 레거시의 talk/, reaction/ 패키지 전체를 참고하여 포팅하십시오. 특히 ReactionBatchWorker와 같은 성능 최적화 로직이나 TalkAutoHiddenProcessor와 같은 자동화 로직을 마이크로서비스 환경에 맞게 이식해야 합니다.
- 새로운 코드를 작성하거나 수정할 때마다 반드시 단위 테스트(Unit Test)를 함께 작성하고 통과를 확인하십시오.
- 테스트 작성 시 Kotlin 환경에 최적화된 업계 표준 라이브러리인 mockito-kotlin 등을 우선적으로 사용하십시오.

### C. 회원 인증 처리

- 인증이 필요한 API는 X-Member-Id 헤더를 파라미터로 받아 사용하십시오.
- 인증 선택적 API(예: 북톡 목록 조회)는 X-Member-Id 헤더가 없어도 동작해야 합니다.
- 헤더가 없으면 비로그인 상태로 처리합니다 (예: 반응 표시 없음).

### D. 숨김(soft delete) 처리

- 북톡 삭제는 물리 삭제가 아닌 숨김(hidden) 처리입니다.
- 레거시의 Talk.java에서 hidden 필드와 처리 방식을 참고하십시오.
- 목록 조회 시 숨겨진 북톡은 제외하십시오.

### E. 메시징 및 이벤트 처리 규칙

- 모든 외부 이벤트 발행은 트랜잭셔널 아웃박스(Transactional Outbox) 패턴을 따릅니다.
- 이벤트 직렬화 포맷은 평면화된 JSON(Flattened JSON)을 사용합니다.
- 메타데이터 필드(ID, 타입, 시간, 애그리거트 ID)에는 `evt_` 접두어를 사용하여 비즈니스 데이터와 구분합니다.
- 발행되는 메시지의 페이로드는 서비스 내 정의된 Integration Event DTO 인스턴스를 사용하십시오.
- 타 서비스로부터 수신하는 이벤트는 해당 서비스의 이벤트 구조에 맞춘 DTO(예: MemberEventDto)를 정의하여 사용하십시오.

### F. 서비스 간 통신

- 북톡 목록 응답에 책 정보(제목, 썸네일)가 필요하면 microservice-book의 내부 API를 호출하십시오.
- 통신 방식: Spring RestClient + Consul LoadBalancer

### G. 문서 규칙

- 마크다운 작성 시 굵게(bold)나 기울임(italics) 같은 강조 서식을 사용하지 않습니다.
- 마크다운 작성 시 이모티콘을 사용하지 않습니다.

## 5. 구현 스펙 참조

[docs/spec.md](./docs/spec.md)를 반드시 읽고 작업을 시작하십시오.
