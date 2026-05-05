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
  config/          CacheConfig.kt, JpaConfig.kt 등
  domain/          Talk.kt, Reaction.kt, ReactionType.kt
  application/
    in/            TalkCommandable.kt, TalkQueryable.kt, ReactionModifiable.kt
    out/           TalkPersistable.kt, TalkLoadable.kt, ReactionPersistable.kt, ReactionLoadable.kt, MemberLoadable.kt, OutboxEventPersistable.kt
  adaptor/
    in/web/        TalkController.kt, ReactionController.kt, SpecController.kt
    out/           TalkJpaRepository.kt, ReactionJpaRepository.kt, external/MemberAdapter.kt, outbox/OutboxPersistenceAdapter.kt
```

## API 명세

### 북톡 API (/api/talks)

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| GET    | /api/talks?memberId= | Required | 특정 회원이 작성한 북톡 목록 (X-Member-Id 헤더와 memberId가 일치해야 함) |
| GET    | /api/talks/book/{bookId} | Optional | 특정 책의 북톡 목록 |
| GET    | /api/talks/recommended | Optional | 추천 북톡 목록 (랜덤) |
| POST   | /api/talks | Required | 북톡 작성 |
| PUT    | /api/talks/{talkId} | Required | 북톡 수정 (작성자만) |
| DELETE | /api/talks/{talkId} | Required | 북톡 숨김 처리 (작성자만) |

### 반응 API (/api/reactions)

| 메서드 | 경로 | 인증 | 설명 |
|---|---|---|---|
| POST   | /api/reactions/talks/{talkId} | Required | 반응 추가 (body에 type 포함) |
| DELETE | /api/reactions/talks/{talkId} | Required | 반응 제거 (body에 type 포함) |

인증: X-Member-Id 헤더 (Gateway가 주입). Optional은 헤더 없어도 동작.

## 도메인 모델

Talk: id(UUID), bookId, memberId, content, hidden(Boolean), dateToHidden(LocalDate)

Reaction: id(UUID), talkId, memberId, type(ReactionType)

## 환경변수 및 보안

모든 민감 정보는 k8s Secret(quietchatter-secrets)으로부터 환경 변수로 주입됩니다.

| 변수명 | 용도 | 비고 |
|---|---|---|
| SERVER_PORT | 서비스 포트 번호 | k8s: 8084 |
| DB_URL | PostgreSQL 접속 URL | |
| DB_USERNAME | PostgreSQL 사용자명 | |
| DB_PASSWORD | PostgreSQL 비밀번호 | |
| INTERNAL_SECRET | 서비스 간 통신용 공유 비밀키 | |
| KAFKA_BROKERS | Kafka 브로커 목록 | |
| SPRING_DATA_REDIS_HOST | Redis 호스트 주소 | |
| SPRING_DATA_REDIS_PORT | Redis 포트 번호 | |
| SPRING_PROFILES_ACTIVE | 활성 프로파일 | prod |

## 비즈니스 규칙

- 북톡 삭제는 물리 삭제가 아닌 hidden=true 처리
- dateToHidden 기본값은 작성/수정 시점으로부터 12개월 후로 설정됨
- 모든 목록 조회 시 hidden=true인 북톡은 자동으로 제외
- 수정/삭제는 작성자 본인만 가능 (불일치 시 403)

## 서비스 간 통신

- 동기 (Feign Client): Talk 작성 시 microservice-member의 내부 API(/internal/api/members/{memberId})를 호출하여 작성자 닉네임 스냅샷을 획득 및 저장. 호출 시 X-Internal-Secret 헤더를 INTERNAL_SECRET env var 값으로 자동 주입.
- k8s DNS: 모든 서비스 호출은 k8s DNS(service.quietchatter.svc.cluster.local)를 사용.

## 에러 핸들링

RFC 7807 (Problem Details for HTTP APIs) 표준을 준수하며, @RestControllerAdvice를 통해 전역 예외 처리를 수행합니다.

## 이벤트

이벤트 포맷: CloudEvents 1.0 (specversion, id, source, type, time, subject, datacontenttype, data). 시각 필드는 RFC 3339(UTC) 형식.

발행 이벤트 (Kafka 토픽: talk):

| type 필드 | 트리거 | data 필드 |
|---|---|---|
| com.quietchatter.talk.TalkHiddenEvent | 자동 만료 숨김 | talkId, reason(AUTO_HIDDEN) |

구독 이벤트 (컨슈머 그룹: microservice-talk-group, Kafka 토픽: member):

| type 필드 | 처리 내용 |
|---|---|
| com.quietchatter.member.MemberDeactivatedEvent | 해당 회원의 모든 북톡 숨김 처리 |
| com.quietchatter.member.MemberProfileUpdatedEvent | 해당 회원의 모든 북톡 닉네임 스냅샷 최신화 |

전송 패턴: Transactional Outbox. OutboxRelayService가 1초 간격으로 미처리 이벤트를 릴레이하고, 처리 완료된 이벤트는 7일 후 자동 삭제(매시간 정각 cleanup job).

## 로컬 실행

사전 요구 사항: Docker, JDK 21

```bash
./gradlew bootRun
```
