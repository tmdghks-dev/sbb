# SBB (점프 투 스프링부트) 복습 프로젝트

『점프 투 스프링부트』의 기능을 구현하면서, 장희성 강사의 수업(14163 점프 투 스프링부트 개선, 14168 JPA·타임리프 개선) 방식을 적용해 개선하는 프로젝트입니다.

- 교재: https://wikidocs.net/book/7601 / https://github.com/pahkey/sbb3
- 14163: https://www.slog.gg/p/14163/raw / https://github.com/jhs512/p-14163-2
- 14168: https://www.slog.gg/p/14168/raw / https://github.com/jhs512/p-14168-2
- 추가 기능: https://wikidocs.net/162814

## 기술 스택

- Spring Boot 4.1.2, Java 25, Gradle 9.8.2 (Kotlin DSL)

## 작업내역

### 001-01 프로젝트 시작, 프로젝트 생성
- [링크](https://github.com/tmdghks-dev/sbb/commit/001-01)
- 작업: Spring Boot 4.1.2 · Java 25 · Gradle Kotlin DSL 프로젝트 생성 (패키지 `com.back`, 메인 클래스 `BackApplication`), 의존성은 webmvc · lombok · devtools · 테스트만 추가, README 작성
- 이유: 강사 14168과 같은 출발점(패키지·빌드 설정)을 만들고, H2·JPA 같은 의존성은 필요한 작업에서 하나씩 추가하기 위해 (14163 진행 방식)
- 결과: `./gradlew test` 통과(contextLoads), 실행 시 8080 포트로 기동, 컨트롤러가 없어서 `GET /`는 404
