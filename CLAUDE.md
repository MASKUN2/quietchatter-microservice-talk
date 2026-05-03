# CLAUDE.md - microservice-talk

작업 전 README.md를 읽으십시오. 서비스 개요, 기술 스택, API 명세, 비즈니스 규칙은 README.md에 있습니다.

루트 프로젝트의 CLAUDE.md에 정의된 공통 원칙도 확인하십시오.

## 작업 지침

### A. 코드 포팅 규칙

- 레거시의 talk/, reaction/ 패키지를 참고하여 idiomatic Kotlin으로 재작성하십시오.
- ReactionBatchWorker, TalkAutoHiddenProcessor 등 성능 최적화/자동화 로직은 k8s 환경에 맞게 이식하십시오.
- 새로운 코드 작성 또는 수정 시 단위 테스트를 함께 작성하고 통과를 확인하십시오.

### B. 회원 인증 처리

- Gateway 계약: 토큰이 있으면 X-Member-Id 헤더가 전달되고, 없으면 헤더가 아예 전송되지 않는다. 빈 문자열은 오지 않는다.
- 인증 필수 엔드포인트는 @RequestHeader("X-Member-Id") memberId: UUID 로 선언한다. 헤더 미포함 시 MissingRequestHeaderException이 발생하며 GlobalExceptionHandler가 401로 처리한다.
- Optional 인증 엔드포인트는 @RequestHeader("X-Member-Id", required = false) memberId: UUID? 로 선언한다. null이면 비로그인 상태로 처리한다 (예: reacted 필드 없음).

### C. 숨김(soft delete) 처리

- 북톡 삭제는 물리 삭제가 아닌 hidden=true 처리다.
- 목록 조회 시 hidden=true인 북톡을 제외한다.

### D. 메시징 규칙

- 모든 이벤트 발행은 Transactional Outbox 패턴을 따른다.
- 이벤트 포맷: CloudEvents 1.0. TalkIntegrationEvent 클래스를 사용하며 필드는 specversion, id, source, type, time, subject, datacontenttype, data를 포함한다.
- type 필드 명명 규칙: com.quietchatter.talk.{EventName} (예: com.quietchatter.talk.TalkHiddenEvent).
- time 필드: LocalDateTime.atOffset(ZoneOffset.UTC).toString()으로 RFC 3339 형식 직렬화.
- 타 서비스 이벤트 수신 시 독립 DTO(예: MemberEventDto)를 정의하여 결합도를 낮추십시오. MemberEventDto는 CloudEvents type, subject, data 필드를 매핑한다.
- Consumer 함수는 Consumer<String>으로 선언하고 함수명을 spring.cloud.stream.function.definition에 등록하십시오. Consumer<Message<T>>는 Spring Cloud Stream 4.x에서 네이티브 핸들러로 간주되어 채널 바인딩이 생성되지 않습니다.

### E. 서비스 간 통신

- book 정보 조회가 필요하면 microservice-book의 /api/books API를 Spring RestClient로 직접 호출한다.
- k8s 환경에서 Consul 없이 k8s DNS(book.quietchatter.svc.cluster.local:8081)를 사용한다.
