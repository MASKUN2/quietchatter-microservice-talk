# microservice-talk (QuietChatter Microservice)

이 저장소는 QuietChatter 프로젝트의 Talk 및 Reaction 도메인을 담당하는 마이크로서비스입니다.
레거시 시스템(legacy-quiet-chatter)의 talk, reaction 패키지 기능들을 코틀린(Kotlin)과 Spring Boot 3 환경으로 포팅하고 고도화하는 역할을 합니다.

## 아키텍처 및 역할
* 언어: Kotlin 1.9.x
* 프레임워크: Spring Boot 3.5.13
* 서비스 탐색: HashiCorp Consul (spring-cloud-starter-consul-discovery)
* 설정 관리: HashiCorp Consul (spring-cloud-starter-consul-config)
* 특징: 사용자 식별이 필요한 경우, API Gateway에서 삽입한 X-User-Id HTTP 헤더 정보를 전적으로 신뢰하여 사용합니다. 내부에서 별도의 JWT 검증을 수행하지 마십시오.

## AI 에이전트 작업 지침
1. 코드 포팅: 레거시 코드를 포팅할 때 Java 코드를 idiomatic한 Kotlin 코드로 변환하십시오.
2. Lombok 금지: Kotlin에서는 Lombok을 사용하지 않습니다. Data class를 적극 활용하십시오.
3. 문서 규칙 준수: 프로젝트 루트의 CONVENTIONS.md에 명시된 대로 마크다운 작성 시 강조 서식(굵게/기울임)과 이모티콘 사용을 엄격히 금지합니다.
