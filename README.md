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

### 001-02 프로젝트 시작, MainController로 `/`·`/hello` 응답
- [링크](https://github.com/tmdghks-dev/sbb/commit/001-02)
- 작업: `MainController`(`@Controller`)에 `GET /` → `"SBB"`, `GET /hello` → `"안녕하세요 ^ ^"` 액션 추가, 각 액션에 `@ResponseBody`
- 이유: 컨트롤러가 없으면 404가 난다는 것을 001-01에서 확인했고, 액션 메서드가 실행되고 반환값이 응답 본문이 되는 흐름을 확인하기 위해 (14163 3~4강). `@ResponseBody`가 없으면 반환 문자열을 템플릿 이름으로 해석하고, void를 반환하면 500 오류가 남
- 결과: `./gradlew test` 통과, 브라우저에서 `/` → `SBB`, `/hello` → `안녕하세요 ^ ^` 확인

### 002-01 개발 환경, H2·H2 콘솔·JPA 의존성 추가
- [링크](https://github.com/tmdghks-dev/sbb/commit/002-01)
- 작업: `spring-boot-starter-data-jpa`, `spring-boot-h2console`, `com.h2database:h2`, 테스트용 `spring-boot-starter-data-jpa-test` 추가
- 이유: 질문·답변을 DB에 저장하기 위한 준비 (책 2-03). Boot 4는 H2 콘솔이 별도 모듈(`spring-boot-h2console`)이라 따로 추가해야 함 (14163·14168)
- 결과: 테스트 통과, 개발 실행 시 `/h2-console` 200. 아직 DB 설정이 없어서 매번 이름이 랜덤인 메모리 DB(`jdbc:h2:mem:fb2e…`)가 만들어짐 → 002-02에서 해결. H2 콘솔은 devtools가 있을 때만 자동으로 켜져서 jar 실행에서는 404
