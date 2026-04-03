# microservice-talk 구현 스펙

## 1. 서비스 역할

북톡(BookTalk)과 반응(Reaction) 관련 모든 기능을 담당합니다.
사용자가 책에 대한 이야기(북톡)를 작성하고, 다른 사람의 북톡에 반응(좋아요 등)을 남길 수 있습니다.

## 2. 도메인 모델

### Talk (북톡)

| 필드 | 타입 | 설명 |
|---|---|---|
| id | UUID | 기본 키 |
| bookId | UUID | 연결된 책 ID (microservice-book 참조) |
| memberId | UUID | 작성자 ID (microservice-member 참조) |
| content | String | 본문 내용 |
| hidden | Boolean | 숨김 여부 (soft delete) |
| dateToHidden | LocalDate | 자동 숨김 날짜 (nullable) |
| createdAt | LocalDateTime | 생성일시 |
| updatedAt | LocalDateTime | 수정일시 |

### Reaction (반응)

| 필드 | 타입 | 설명 |
|---|---|---|
| id | UUID | 기본 키 |
| talkId | UUID | 연결된 북톡 ID |
| memberId | UUID | 반응한 사용자 ID |
| type | ReactionType | 반응 종류 (LIKE 등) |

## 3. API 명세

### 3.1 북톡 조회 API (인증 선택)

#### GET /v1/talks?bookId={bookId}
특정 책의 북톡 목록을 페이지네이션으로 조회합니다.

쿼리 파라미터:
* `bookId` (필수): 책 UUID
* `page`, `size`: 페이지네이션

처리 규칙:
* 숨겨진(hidden=true) 북톡은 제외합니다.
* `X-Member-Id` 헤더가 있으면: 해당 사용자의 반응 여부(reacted)를 포함하여 응답합니다.
* `X-Member-Id` 헤더가 없으면: 반응 여부 없이 응답합니다.

응답:
```json
{
  "content": [
    {
      "id": "uuid",
      "bookId": "uuid",
      "memberId": "uuid",
      "content": "이 책은 정말 좋아요",
      "reactionCount": 5,
      "reacted": true,
      "createdAt": "2024-01-01T12:00:00"
    }
  ],
  "totalPages": 3,
  "totalElements": 50
}
```

#### GET /v1/talks/recommend
추천 북톡 목록을 조회합니다. 최근 반응이 많은 북톡을 기준으로 반환합니다.

쿼리 파라미터: `size` (기본값: 5)

응답: 북톡 목록 (배열)

### 3.2 북톡 작성/수정/삭제 API (인증 필요)

#### POST /v1/talks
북톡을 새로 작성합니다.

요청 헤더: `X-Member-Id: {memberId}`

요청 바디:
```json
{
  "bookId": "uuid",
  "content": "북톡 내용",
  "hidden": "2024-12-31T00:00:00Z"
}
```

* `hidden`: 자동으로 숨김 처리할 날짜 (nullable, ISO 8601 형식)

응답:
```json
{
  "id": "uuid"
}
```

#### PUT /v1/talks/{talkId}
북톡 내용을 수정합니다. 작성자 본인만 수정 가능합니다.

요청 헤더: `X-Member-Id: {memberId}`

요청 바디:
```json
{
  "content": "수정된 내용"
}
```

응답: 204 No Content

#### DELETE /v1/talks/{talkId}
북톡을 숨김 처리합니다. 작성자 본인만 삭제(숨김) 가능합니다.

요청 헤더: `X-Member-Id: {memberId}`

응답: 204 No Content

### 3.3 반응 API (인증 필요)

#### POST /v1/reactions
북톡에 반응을 추가합니다.

요청 헤더: `X-Member-Id: {memberId}`

요청 바디:
```json
{
  "talkId": "uuid",
  "type": "LIKE"
}
```

응답: 202 Accepted

#### DELETE /v1/reactions
북톡에서 반응을 제거합니다.

요청 헤더: `X-Member-Id: {memberId}`

요청 바디:
```json
{
  "talkId": "uuid",
  "type": "LIKE"
}
```

응답: 202 Accepted

## 4. 내부 API (서비스 간 통신용)

### GET /internal/talks/by-member/{memberId}
특정 회원이 작성한 북톡 목록 조회. (microservice-member의 /v1/me/talks 에서 사용)

응답: 페이지네이션된 Talk 목록

### DELETE /internal/talks/by-member/{memberId}
특정 회원의 모든 북톡을 숨김 처리. (microservice-member의 회원 탈퇴 시 사용)

응답: 204 No Content

## 5. 비즈니스 규칙

### 북톡 작성 제한

* 동일 사용자가 동일 책에 북톡을 하나만 작성할 수 있습니다.
* 이미 작성한 경우 409 Conflict 응답.

### 수정/삭제 권한

* 작성자 본인(memberId == X-Member-Id)만 수정 및 삭제 가능합니다.
* 권한이 없는 경우 403 Forbidden 응답.

### 자동 숨김

* `dateToHidden`이 설정된 북톡은 해당 날짜 이후 조회 시 자동으로 숨겨집니다.
* 별도 배치 처리 없이 조회 시 필터링으로 구현합니다.

## 6. 구현 우선순위

1. Talk, Reaction 도메인 및 JPA 설정
2. 북톡 목록 조회 API (책별, 추천)
3. 북톡 작성/수정/삭제 API
4. 반응 추가/제거 API
5. 내부 API (회원별 조회, 일괄 숨김)
