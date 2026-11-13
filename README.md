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

### 002-02 개발 환경, 프로필별 DB 분리 (dev 파일 DB / test 메모리 DB)
- [링크](https://github.com/tmdghks-dev/sbb/commit/002-02)
- 작업: `application.yaml`(공통, 기본 프로필 `dev`, `ddl-auto: update`) / `application-dev.yaml`(`jdbc:h2:./db_dev;MODE=MySQL`) / `application-test.yaml`(`jdbc:h2:mem:db_test;MODE=MySQL`)로 분리, 테스트에 `@ActiveProfiles("test")`, `.gitignore`에 `db_dev.*` 추가
- 이유: 책은 개발과 테스트가 같은 DB(`~/local`)를 써서 테스트가 개발 데이터를 오염시킴. 테스트는 매번 새로 만들어지는 메모리 DB를 쓰도록 분리 (14163 5강, 14168). `MODE=MySQL`은 나중에 MySQL로 바꿔도 SQL 문법 차이를 줄이기 위함 (14168)
- 결과: 테스트는 `test` 프로필 + `jdbc:h2:mem:db_test`, 개발 실행은 `dev` 프로필 + 프로젝트 폴더의 `db_dev.mv.db` 파일 생성, H2 콘솔로 `jdbc:h2:./db_dev` 접속 확인, DB 파일은 git에 안 올라감

### 002-03 개발 환경, SQL·트랜잭션 로그 설정
- [링크](https://github.com/tmdghks-dev/sbb/commit/002-03)
- 작업: `show-sql`, `format_sql`, `highlight_sql`, `use_sql_comments` 켜기, `org.hibernate.orm.jdbc.bind`·`extract` TRACE(바인딩 값·조회 결과), `org.springframework.transaction.interceptor` TRACE(트랜잭션 시작·종료), 컬러 로그(`output.ansi.enabled: always`)
- 이유: JPA가 실제로 어떤 SQL을 언제 실행하는지, `?`에 어떤 값이 들어가는지, 트랜잭션이 어디서 열리고 닫히는지 눈으로 확인하면서 공부하기 위해 (14163 6강, 14168)
- 결과: 테스트 통과. 아직 엔티티가 없어서 실행되는 SQL이 없음 → 003(엔티티)부터 로그로 확인

### 002-04 개발 환경, 커넥션 auto-commit 끄기
- [링크](https://github.com/tmdghks-dev/sbb/commit/002-04)
- 작업: `spring.datasource.hikari.auto-commit: false`
- 이유: 커넥션 풀이 기본으로 auto-commit을 켜 두면 SQL 하나마다 바로 커밋됨. 커밋 시점을 `@Transactional`(이 프로젝트는 컨트롤러 액션에 붙임)이 정하도록 맡기고, 트랜잭션 밖에서 실수로 반영되는 것을 막기 위해 (14168)
- 결과: 테스트 통과, 앱 정상 기동(`/` 200). 실제 효과는 004(리포지터리·트랜잭션 테스트)에서 로그로 확인

### 003-01 엔티티, BaseEntity와 JPA Auditing
- [링크](https://github.com/tmdghks-dev/sbb/commit/003-01)
- 작업: global/jpa/entity/BaseEntity 추가: @MappedSuperclass, int id(IDENTITY), @CreatedDate createDate, @LastModifiedDate modifyDate, id 기반 equals/hashCode, BackApplication에 @EnableJpaAuditing
- 이유: 책은 Question·Answer마다 id·createDate를 중복 선언하고 LocalDateTime.now()를 직접 넣음, 공통 필드를 부모 클래스로 모으고 날짜는 Auditing이 자동으로 채우게 하기 위해 (14168), equals/hashCode는 컬렉션에서 remove가 정확히 동작하게 하기 위함 (14168 38강). 주의: id만 비교하므로 저장 전(id=0) 엔티티끼리는 같다고 판단됨
- 결과: ./gradlew test 통과 (1개), 아직 상속하는 엔티티가 없어 테이블은 생기지 않음

### 003-02 엔티티, Question 생성
- [링크](https://github.com/tmdghks-dev/sbb/commit/003-02)
- 작업: domain/question/question/entity/Question: BaseEntity 상속, subject(length 200), content(TEXT), @Getter·@NoArgsConstructor만 쓰고 @Setter 없음, 생성자 Question(subject, content)로 생성, 패키지는 처음부터 14168 구조(domain/애그리거트/엔티티/계층) 사용
- 이유: 책은 @Setter를 열어 두고 서비스에서 new 후 setter로 값을 채움 → 어디서나 값이 바뀔 수 있음, 생성은 생성자로, 변경은 나중에 도메인 메서드(modify)로만 하게 하기 위해 (14168 Post)
- 결과: ./gradlew test 통과 (1개), SQL 로그에 create table question (id, create_date, modify_date, subject varchar(200), content text) 확인

### 003-03 엔티티, Answer 생성 (ManyToOne LAZY)
- [링크](https://github.com/tmdghks-dev/sbb/commit/003-03)
- 작업: domain/question/answer/entity/Answer: BaseEntity 상속, content(TEXT), @ManyToOne(fetch = LAZY) question → answer 테이블에 question_id 외래키, setter 없이 생성자 Answer(question, content)
- 이유: 책은 @ManyToOne 기본값(EAGER)이라 답변을 조회할 때마다 질문까지 즉시 함께 조회함, 필요할 때만 조회하도록 LAZY를 명시 (14163 22~25강, 14168: 웬만하면 LAZY만 사용)
- 결과: ./gradlew test 통과 (1개), SQL 로그에 create table answer (question_id 포함)와 question_id 외래키 제약 생성 확인

### 003-04 엔티티, 질문→답변 목록과 addAnswer (cascade PERSIST·REMOVE)
- [링크](https://github.com/tmdghks-dev/sbb/commit/003-04)
- 작업: Question에 @OneToMany(mappedBy = "question", cascade = {PERSIST, REMOVE}) answers 추가, new ArrayList<>()로 초기화, 연관관계 편의 메서드 addAnswer(content): Answer를 만들어 answers에 추가하고 반환
- 이유: mappedBy가 없으면 JPA가 중간 테이블(question_answers)을 만듦 → 외래키는 answer.question_id 하나로 관리 (14163 007~013), PERSIST: 질문에 답변을 추가만 해도 저장(INSERT)되게, REMOVE: 질문을 지우면 답변도 지워지게, 책은 answerRepository.save로 저장 → 답변은 질문을 통해서만 다루기 위해 addAnswer 사용 (14163 37강 강사의 커스터마이징, 14168 addComment), 컬렉션 초기화는 NPE 방지 (14163)
- 결과: ./gradlew test 통과 (1개), SQL 로그에 중간 테이블(question_answers)이 생기지 않음 확인, addAnswer 동작은 004-05 테스트에서 확인

### 003-05 엔티티, orphanRemoval 추가
- [링크](https://github.com/tmdghks-dev/sbb/commit/003-05)
- 작업: Question.answers에 orphanRemoval = true 추가
- 이유: answers 리스트에서 답변을 빼기만 하면(remove) 그 답변이 DB에서도 DELETE 되게 하기 위해 (14168 Post.comments), cascade REMOVE는 부모(질문)를 지울 때 자식도 지우는 것이고, orphanRemoval은 부모와의 관계가 끊긴 자식을 지우는 것 (14168 강의 표), 답변 삭제(015-05)를 AnswerRepository 없이 question.deleteAnswer()로 처리하기 위한 준비
- 결과: ./gradlew test 통과 (1개), 실제 삭제 동작은 015-05에서 확인

### 004-01 리포지터리, QuestionRepository와 쿼리 메서드
- [링크](https://github.com/tmdghks-dev/sbb/commit/004-01)
- 작업: QuestionRepository extends JpaRepository<Question, Integer>, 쿼리 메서드 findBySubject, findBySubjectAndContent(Optional 반환), findBySubjectLike(List 반환)
- 이유: 메서드 이름만으로 SELECT 쿼리를 만드는 쿼리 메서드 명명 규칙 학습 (책 2-05, 14163 015~017), 하나만 나오는 조회는 Optional로 받아 없는 경우를 명확히 처리하기 위해 (14163)
- 결과: ./gradlew test 통과 (1개), 각 메서드 동작은 004-03 테스트에서 확인
