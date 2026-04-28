# microservice-talk

QuietChatter의 북톡(BookTalk) 및 반응(Reaction) 도메인 서비스. 회원이 책에 대한 이야기를 작성하고 반응을 남길 수 있다.

## 기술 스택

- 언어: Kotlin 1.9.25
- 프레임워크: Spring Boot 3.5.13
- 런타임: JDK 21 Virtual Threads 활성화
- 데이터베이스: PostgreSQL (JPA, Flyway)
- 메시징: Spring Cloud Stream + Kafka (Redpanda)
- 포트: 8084 (k8s 배포 시 SERVER_PORT 환경변수로 주입, 로컬 기본값 8080)

## 패키지 구조

헥사고날 아키텍처.

```
com.quietchatter.talk/
  domain/          Talk.kt, Reaction.kt, ReactionType.kt
  application/
    in/            TalkCommandable.kt, TalkQueryable.kt, ReactionModifiable.kt
    out/           TalkRepository.kt, ReactionRepository.kt
  adaptor/
    in/web/        TalkController.kt, ReactionController.kt, SpecController.kt
    out/           TalkJpaRepository.kt, ReactionJpaRepository.kt
```

## API 명세

### 북톡 API (/api/talks)

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| GET    | /api/talks | Optional | 전체 북톡 목록 (페이지네이션) |
| GET    | /api/talks/book/{bookId} | Optional | 특정 책의 북톡 목록 |
| GET    | /api/talks/recommended | Optional | 추천 북톡 목록 (반응 많은 순) |
| POST   | /api/talks | Required | 북톡 작성 |
| PATCH  | /api/talks/{talkId} | Required | 북톡 수정 (작성자만) |
| DELETE | /api/talks/{talkId} | Required | 북톡 숨김 처리 (작성자만) |

### 반응 API (/api/reactions)

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| POST   | /api/reactions/talks/{talkId} | Required | 반응 추가. 응답: 202 Accepted |
| DELETE | /api/reactions/talks/{talkId} | Required | 반응 제거. 응답: 202 Accepted |

인증: X-Member-Id 헤더 (Gateway가 주입). Optional은 헤더 없어도 동작.

## 도메인 모델

Talk: id(UUID), bookId, memberId, content, hidden(Boolean), dateToHidden(LocalDate?)

Reaction: id(UUID), talkId, memberId, type(ReactionType)

## 비즈니스 규칙

- 동일 회원이 동일 책에 북톡 하나만 작성 가능 (중복 시 409)
- 북톡 삭제는 물리 삭제가 아닌 hidden=true 처리
- dateToHidden이 설정된 북톡은 해당 날짜 이후 조회에서 자동 제외 (배치 없이 필터링)
- 수정/삭제는 작성자 본인만 가능 (불일치 시 403)

## 이벤트

- 발행: TalkIntegrationEvent (Kafka 토픽: talk)
- 구독: member 토픽의 MemberDeactivatedEvent → 해당 회원의 모든 북톡 숨김 처리
- 전송 패턴: Transactional Outbox

## 로컬 실행

사전 요구 사항: Docker, JDK 21

```bash
./gradlew bootRun
```
