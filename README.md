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

### 004-02 초기 데이터, BaseInitData로 샘플 질문·답변 생성
- [링크](https://github.com/tmdghks-dev/sbb/commit/004-02)
- 작업: global/initData/BaseInitData(@Configuration): ApplicationRunner 빈이 앱 시작 시 self.work1() 호출, work1(@Transactional): 질문이 하나라도 있으면 건너뛰고, 질문 2개 저장 후 2번 질문에 addAnswer로 답변 2개 추가, @Autowired @Lazy self로 자기 자신의 프록시를 주입받음
- 이유: 개발 DB와 테스트 DB에 같은 샘플 데이터를 준비해서 화면 확인과 테스트 기준값으로 쓰기 위해 (14168 BaseInitData, 14163 TestInitData), 같은 클래스 안에서 this.work1()을 부르면 프록시를 거치지 않아 @Transactional이 적용되지 않음 → self 프록시로 호출, count() > 0 이면 건너뛰어 재시작해도 중복 생성되지 않게, 아직 Service가 없어서 Repository로 시작하고 005-02에서 Service 호출로 바꿈
- 결과: ./gradlew test 통과 (1개), 로그에 work1 트랜잭션 시작 → insert question 2번 → insert answer 2번 → 트랜잭션 종료 확인 (answer는 save 없이 cascade PERSIST로 INSERT)

### 004-03 리포지터리 테스트, 조회 테스트 (test 프로필 + 트랜잭션 롤백)
- [링크](https://github.com/tmdghks-dev/sbb/commit/004-03)
- 작업: QuestionRepositoryTest에 @ActiveProfiles("test") @SpringBootTest @Transactional, t1 findAll(2건), t2 findById, t3 findBySubject, t4 findBySubjectAndContent, t5 findBySubjectLike
- 이유: 책의 SbbApplicationTests는 개발 DB에 데이터를 넣기만 하고 assert가 없음, test 프로필(메모리 DB)에서 BaseInitData가 만든 데이터를 기준으로 결과를 단언 (14163 014~017), @Transactional: 테스트마다 끝나면 롤백되어 테스트끼리 서로 영향을 주지 않게 (14163 018)
- 결과: ./gradlew test 통과 (6개)

### 004-04 리포지터리 테스트, modify 도메인 메서드와 수정 테스트 (dirty checking)
- [링크](https://github.com/tmdghks-dev/sbb/commit/004-04)
- 작업: Question.modify(subject, content) 도메인 메서드 추가, t6: modify만 호출하고 save 없이 findBySubject("수정된 제목")로 수정 확인, t7: t6의 수정이 롤백되어 원래 제목이 그대로인지 확인
- 이유: 책은 setSubject 후 save로 수정 → setter 없이 엔티티 스스로 값을 바꾸는 메서드로 변경 (14168 Post.modify), 트랜잭션 안에서 바뀐 엔티티는 save 없이도 UPDATE 됨(dirty checking)을 확인 (14168), 14163 018-1에서 보여준 '수정 테스트가 다른 테스트를 깨뜨리는 문제'가 @Transactional 롤백으로 해결됨을 확인 (14163 018-2)
- 결과: ./gradlew test 통과 (8개), 로그에 save 호출 없이 update question 실행, 테스트 종료 시 롤백 확인

### 004-05 리포지터리 테스트, 삭제·addAnswer·LAZY 로딩 테스트
- [링크](https://github.com/tmdghks-dev/sbb/commit/004-05)
- 작업: t8 삭제: delete 후 count 2 → 1 (답변이 달린 2번이 아니라 1번 질문), t9 addAnswer: flush 전에는 id 0, flush 후 cascade PERSIST로 INSERT되어 id 생성, t10 LAZY 로딩: question.getAnswers() 호출 시점에 답변 2개 조회, 14163의 answerRepository 테스트(t8, t10)는 AnswerRepository를 두지 않기로 해서 제외
- 이유: 답변을 save 없이 질문에 추가만 해도 저장되는 cascade PERSIST를 테스트로 확인 (14163 019~021), LAZY 컬렉션은 실제로 사용할 때 SELECT되며, 트랜잭션(커넥션)이 살아 있어야 로딩된다는 것을 확인 (14163 022~025), flush를 직접 호출해 INSERT 시점을 눈으로 확인 (14163 강의: INSERT 로그를 보려면 flush)
- 결과: ./gradlew test 통과 (11개)

### 005-01 질문 목록, QuestionService와 GET /questions
- [링크](https://github.com/tmdghks-dev/sbb/commit/005-01)
- 작업: 타임리프 의존성 추가, dev 프로필에 thymeleaf cache false + prefix file:src/main/resources/templates/, QuestionService(count, write, findById, findAll), QuestionController GET /questions → question/question/list 템플릿, 액션에 @Transactional(readOnly = true), BaseInitData를 Repository 대신 QuestionService 호출로 변경
- 이유: 컨트롤러는 Service를 통해 데이터를 다루게 하기 위해 (책 2-09, 14168 PostService), URL은 복수형 명사 /questions (14163 37강 표, 14168 /posts), 트랜잭션은 DB에 접근하는 컨트롤러 액션에 두고 조회는 readOnly (14168), 템플릿 경로는 도메인 구조와 같게 question/question/list (14168 post/post/list), dev에서 템플릿을 소스 폴더에서 바로 읽어 재시작 없이 반영 (14168)
- 결과: ./gradlew test 통과 (11개), 브라우저(ego-lite)에서 /questions에 샘플 질문 2개(번호·제목·작성일시) 표시 확인

### 005-02 질문 목록, 루트와 /questions/ 리다이렉트
- [링크](https://github.com/tmdghks-dev/sbb/commit/005-02)
- 작업: MainController GET / → redirect:/questions (@ResponseBody 제거), QuestionController GET /questions/ → redirect:/questions
- 이유: 루트 주소로 들어오면 질문 목록을 보여주기 위해 (책 2-08, 14163 MainController), 끝에 /가 붙은 주소도 목록으로 보내 주소 통일 (14168 /posts/ → /posts), 리다이렉트 액션은 DB에 접근하지 않으므로 @Transactional을 붙이지 않음 (14168)
- 결과: ./gradlew test 통과 (11개), GET / → 302 Location /questions, GET /questions/ → 302 Location /questions 확인

### 005-03 질문 상세, GET /questions/{id}와 없는 질문 404
- [링크](https://github.com/tmdghks-dev/sbb/commit/005-03)
- 작업: QuestionController GET /questions/{id} (@PathVariable int id, readOnly 트랜잭션) → question/question/detail, global/exception/DataNotFoundException(@ResponseStatus 404, reason entity not found), 질문이 없으면 orElseThrow로 던짐, 상세 화면: 제목, 내용(pre-line), 답변 개수와 답변 목록, 목록의 제목을 상세 링크로 변경
- 이유: URL은 /questions/{id} (14163 37강 표, 14168 /posts/{id}), findById().get()은 없는 id에서 NoSuchElementException(500)이 나므로 의미 있는 404로 응답하기 위해 (책 2-10, 14163 35강), @PathVariable은 int로 통일 (14168), 답변 목록은 LAZY 컬렉션이라 화면을 그릴 때 조회됨 (OSIV)
- 결과: ./gradlew test 통과 (11개), 브라우저에서 /questions/2 → 제목·내용·답변 2개 표시, /questions/999 → 404 entity not found 확인

### 006-01 답변 등록, POST /questions/{id}/answers/write (addAnswer, save 없음)
- [링크](https://github.com/tmdghks-dev/sbb/commit/006-01)
- 작업: domain/question/answer/controller/AnswerController: POST /questions/{questionId}/answers/write, @Transactional, QuestionService.writeAnswer(question, content) → question.addAnswer(content), 상세 화면에 답변 입력 폼(textarea + 답변등록) 추가
- 이유: 책은 POST /answer/create/{id} + AnswerService + answerRepository.save → 답변은 질문의 하위 리소스이므로 /questions/{id}/answers/write (14168 /posts/{postId}/comments/write), 답변은 질문을 통해서만 다루기로 해서 save 없이 addAnswer만 호출하고, 트랜잭션이 끝날 때 cascade PERSIST로 INSERT (14163 37강, 14168 writeComment), 처리 후 상세 페이지로 리다이렉트(PRG)해서 새로고침 시 중복 등록 방지 (14168 28강)
- 결과: ./gradlew test 통과 (11개), 브라우저에서 1번 질문에 답변을 입력하고 등록 → /questions/1로 리다이렉트, 1개의 답변 표시, 로그에 save 호출 없이 insert answer 실행 확인

### 007-01 화면, Bootstrap(cdnjs)·Pretendard 적용
- [링크](https://github.com/tmdghks-dev/sbb/commit/007-01)
- 작업: 목록·상세 템플릿에 cdnjs Bootstrap 5.3.8 CSS와 static/style.css 연결, style.css: Pretendard 폰트 @font-face(jsdelivr), body 기본 폰트 지정, 목록은 table, 상세는 card로 질문·답변·작성일시 표시, 답변 폼은 form-control·btn
- 이유: 책은 bootstrap.min.css를 static 폴더에 직접 넣음 → 파일 관리 없이 cdnjs URL을 복사해 바로 사용 (14163 38~39강), 한글 가독성을 위해 Pretendard 폰트 적용 (14163)
- 결과: ./gradlew test 통과 (11개), 브라우저에서 목록 표와 상세 카드가 Bootstrap 스타일로 표시되는 것 확인

### 007-02 화면, layout.html 공통 레이아웃 (layout-dialect)
- [링크](https://github.com/tmdghks-dev/sbb/commit/007-02)
- 작업: thymeleaf-layout-dialect 의존성 추가 (버전은 Boot BOM이 관리), templates/layout.html: doctype·head·Bootstrap·style.css를 한 곳에 두고 layout:fragment="content" 자리에 페이지 내용을 끼움, 목록·상세는 layout:decorate="~{layout}"로 레이아웃 사용, 페이지마다 반복하던 link 태그 제거
- 이유: 페이지마다 head와 CSS 링크를 반복하지 않고, 표준 HTML 구조(doctype, head, body)를 한 곳에서 관리하기 위해 (책 2-15, 14168 44강)
- 결과: ./gradlew test 통과 (11개), 브라우저에서 상세 화면이 레이아웃을 거쳐 Bootstrap·Pretendard가 그대로 적용되고 문서 제목이 SBB로 표시됨 확인

### 007-03 화면, 페이지별 title (layout:title-pattern)
- [링크](https://github.com/tmdghks-dev/sbb/commit/007-03)
- 작업: layout.html의 title에 layout:title-pattern="$CONTENT_TITLE ,  $LAYOUT_TITLE", 목록 페이지 title "질문 목록", 상세 페이지 title "{id}번 질문 - {제목}"
- 이유: 레이아웃을 쓰면 모든 페이지 제목이 같아지는 문제를 해결하고, 브라우저 탭에서 페이지를 구분하기 위해 (14168 45강)
- 결과: ./gradlew test 통과 (11개), 브라우저 탭 제목: 목록 "질문 목록 ,  SBB", 상세 "2번 질문 - 스프링부트 모델 질문입니다. ,  SBB" 확인

### 008-01 질문 등록, GET·POST /questions/write (같은 URL, 메서드로 구분)
- [링크](https://github.com/tmdghks-dev/sbb/commit/008-01)
- 작업: QuestionController에 중첩 record WriteForm(subject, content), GET /questions/write: @ModelAttribute("form")로 빈 폼을 모델에 넣고 write 템플릿 표시, POST /questions/write: 저장 후 redirect:/questions/{id}, write.html: th:action(현재 URL로 전송), th:object="${form}", th:field="*{subject}", 목록에 질문 등록하기 버튼
- 이유: 책은 GET /question/create + POST /question/create → 폼 보기와 처리를 같은 URL에 두고 HTTP 메서드로만 구분 (14168 26~28강), GET에서도 form을 모델에 넣어 두면 템플릿에서 ${form?.subject} 대신 *{subject}로 일관되게 쓸 수 있음 (14168), 폼 DTO는 컨트롤러 안 record로 (14168 40강), 폼 보기 액션은 DB에 접근하지 않아 @Transactional 없음 (14168 showWrite)
- 결과: ./gradlew test 통과 (11개), 브라우저에서 목록 → 질문 등록하기 → 제목·내용 입력 → 저장 → 새 질문 상세로 이동 확인

### 008-02 질문 등록, 서버 입력값 검증 (@Valid + BindingResult)
- [링크](https://github.com/tmdghks-dev/sbb/commit/008-02)
- 작업: spring-boot-starter-validation(+ validation-test) 의존성 추가, WriteForm에 @NotBlank(제목·내용), @Size(max = 200)(제목), POST 액션에 @ModelAttribute("form") @Valid WriteForm form, BindingResult bindingResult (순서 중요), 오류가 있으면 같은 폼 화면을 다시 보여줌, write.html에 #fields.allErrors()로 오류 메시지 표시
- 이유: 빈 제목·내용이 저장되는 것을 서버에서 막기 위해 (책 2-16, 14168 9~15강: 서버 검증은 무조건), BindingResult가 @Valid 바로 뒤에 있어야 예외 대신 오류 정보를 받아 폼을 다시 보여줄 수 있음 (14168), @ModelAttribute("form") 덕분에 다시 보여준 폼에 입력했던 값이 그대로 남음 (14168 입력값 유지), 책의 @NotEmpty는 공백만 입력해도 통과 → @NotBlank (14168)
- 결과: ./gradlew test 통과 (11개), 브라우저에서 제목만 입력하고 저장 → 같은 화면에 "내용을 입력해주세요." 표시, 입력한 제목은 유지됨 확인

### 008-03 질문 등록, 오류 메시지 fragment·정렬·첫 오류 필드 focus
- [링크](https://github.com/tmdghks-dev/sbb/commit/008-03)
- 작업: templates/form_errors.html fragment: 오류를 정렬해서 출력하고, 첫 오류 필드에 focus 주는 스크립트 포함, 검증 메시지를 "01-subject-제목을 입력해주세요."처럼 순번-필드명-메시지 형식으로 변경, write.html에서 th:replace="~{form_errors :: formErrorsFragment}"로 사용
- 이유: #fields.allErrors()는 순서가 매번 달라질 수 있어서 메시지 앞 순번으로 정렬해 항상 같은 순서로 보여주기 위해 (14168 20~25강), 필드명을 data 속성에 담아 첫 오류 필드로 커서를 옮겨 바로 고칠 수 있게 (14168), 오류 표시 코드를 fragment로 분리해 다른 폼에서도 재사용 (책 2-16 form_errors, 14168)
- 결과: ./gradlew test 통과 (11개), 빈 폼 저장을 3번 반복해도 항상 "제목 → 내용" 순서로 표시되고 제목에 focus 확인, 단 제목만 입력했을 때 내용 칸으로 focus가 가지 않는 문제 발견 → 008-04에서 수정

### 008-04 질문 등록, 첫 오류 필드 focus가 autofocus에 밀리던 문제 수정
- [링크](https://github.com/tmdghks-dev/sbb/commit/008-04)
- 작업: form_errors.html의 focus 스크립트를 DOMContentLoaded 이후에 실행, focus 전에 폼 안의 autofocus 속성 제거
- 이유: fragment 스크립트는 폼 맨 위에서 실행되어 아래쪽 입력칸이 아직 없고, 제목칸의 autofocus가 나중에 적용되어 focus를 빼앗음 (14168 코드 그대로일 때 생기는 문제), 문서를 다 읽은 뒤 autofocus를 지우고 첫 오류 필드에 focus를 줘야 의도대로 동작
- 결과: ./gradlew test 통과 (11개), 브라우저에서 제목만 입력하고 저장 → 내용 칸에 focus, 빈 폼 저장 → 제목 칸에 focus 확인

### 008-05 답변 등록, 클라이언트 JS 검사 + 서버 @Valid
- [링크](https://github.com/tmdghks-dev/sbb/commit/008-05)
- 작업: AnswerController에 중첩 record WriteForm(@NotBlank content), 액션 파라미터를 @Valid WriteForm으로 변경 (BindingResult 없음), 상세 화면 답변 폼에 onsubmit JS 검사: 앞뒤 공백 제거 후 비어 있으면 alert와 focus, 통과하면 전송
- 이유: 14168 댓글과 같은 방식: 클라이언트 검사를 먼저 해서 대부분의 실수를 화면에서 막고, 서버 검사는 무조건 함 (14168 33~39강), BindingResult가 없으므로 JS를 우회해 빈 값을 보내면 서버가 400으로 거절 (설계 16), 책은 답변 오류 시 상세 화면을 다시 그림 → 14168 방식을 따름
- 결과: ./gradlew test 통과 (11개), 브라우저에서 공백만 입력하고 답변등록 → "답변 내용을 입력해주세요." alert, 페이지 이동 없음, JS를 거치지 않고 빈 내용을 POST하면 400 응답 확인

### 009-01 내비게이션 바, navbar fragment
- [링크](https://github.com/tmdghks-dev/sbb/commit/009-01)
- 작업: templates/navbar.html: SBB 로고(홈 링크)와 질문 목록 링크, 좁은 화면용 햄버거 버튼, layout.html에서 th:replace="~{navbar :: navbarFragment}"로 모든 페이지 상단에 포함, 햄버거 메뉴 동작을 위해 cdnjs Bootstrap JS(bundle) 추가
- 이유: 어느 페이지에서든 홈과 질문 목록으로 이동하기 위해 (책 3-01), 레이아웃에 한 번만 넣어 모든 페이지에 적용, 책은 bootstrap.min.js를 static에 직접 넣음 → CSS와 같이 cdnjs 사용 (14163 방식), 책 순서대로 질문 등록(2-16) 다음에 진행
- 결과: ./gradlew test 통과 (11개), 브라우저에서 모든 페이지 상단에 navbar 표시, 폭 400px에서 햄버거 버튼을 누르면 메뉴가 펼쳐짐 확인

### 010-01 페이징, 개발 환경용 테스트 데이터 300개
- [링크](https://github.com/tmdghks-dev/sbb/commit/010-01)
- 작업: BaseInitData에 work2 추가: 질문 300개("테스터 데이터입니다:[001]" ~ [300]) 생성, 이미 300개 이상이면 건너뜀, work2를 실행하는 ApplicationRunner 빈은 @Profile("dev")로 개발 환경에서만, @Order로 work1 다음에 실행
- 이유: 페이징을 확인하려면 데이터가 많아야 함 (책 3-02), 책은 테스트 코드로 개발 DB에 300개를 넣음 → 이 프로젝트의 테스트는 메모리 DB를 쓰고 질문 2개를 기준으로 단언하므로, 개발 환경 초기 데이터로 생성, 강사의 BaseInitData 구조(self 프록시 + @Transactional) 그대로 사용
- 결과: ./gradlew test 통과 (11개, test 프로필에서는 work2가 실행되지 않음), 개발 실행 시 질문 목록에 302개가 한 페이지에 모두 표시됨 확인 → 010-02에서 페이징

### 010-02 페이징, Page<Question>과 최신순 정렬
- [링크](https://github.com/tmdghks-dev/sbb/commit/010-02)
- 작업: QuestionService.findPage(page): PageRequest.of(page, 10, createDate 내림차순)으로 JpaRepository.findAll(Pageable) 호출, 목록 액션에 @RequestParam(defaultValue = "0") int page, 모델에 paging(Page<Question>) 전달, 리포지터리 테스트 t11: 크기 1 페이지에서 전체 2건·2페이지·최신 질문이 먼저 나오는지 확인
- 이유: 질문이 많아지면 한 화면에 다 보여줄 수 없으므로 10개씩 나눠 보여주기 위해 (책 3-02), 최근 질문이 위로 오도록 작성일 내림차순, Page에는 내용뿐 아니라 전체 개수·페이지 수가 들어 있어 페이지 이동 링크를 만들 수 있음
- 결과: ./gradlew test 통과 (12개), 브라우저에서 /questions → [300]부터 10개, /questions?page=1 → [290]부터 10개 확인

### 010-03 페이징, 페이지 이동·게시물 번호·답변 개수
- [링크](https://github.com/tmdghks-dev/sbb/commit/010-03)
- 작업: 목록 아래 Bootstrap pagination: 이전/다음(처음·마지막 페이지에서 비활성), 현재 페이지 기준 앞뒤 5페이지까지 번호 표시, 현재 페이지 active, 게시물 번호 = 전체 개수 - (현재 페이지 × 페이지 크기) - 나열 인덱스, 제목 옆에 답변 개수(#lists.size(question.answers))를 빨간 숫자로 표시, 답변이 없으면 숨김
- 이유: 페이지를 이동할 수 있게 하고, 페이지 번호가 너무 많이 나열되지 않게 범위를 제한 (책 3-02), loop.count는 페이지마다 1부터 다시 시작하므로, 전체 기준 역순 번호를 계산 (책 3-03), 답변이 달린 질문을 목록에서 바로 알 수 있게 (책 3-04), 페이지 번호는 책과 같이 0부터 표시 (1부터 표시는 선택 목록), 주의: 답변 개수를 행마다 조회하므로 N+1 쿼리 발생 (개선은 선택 목록)
- 결과: ./gradlew test 통과 (12개), 브라우저에서 마지막 페이지(30): 번호 2,1, 2번 질문 옆 답변 수 2, 페이지 25~30 표시·다음 비활성, 10페이지: 5~15 표시, 10 active 확인

### 011-01 시큐리티, Spring Security 도입 (일단 전체 허용)
- [링크](https://github.com/tmdghks-dev/sbb/commit/011-01)
- 작업: spring-boot-starter-security(+ security-test) 의존성 추가, global/security/SecurityConfig: SecurityFilterChain에서 /favicon.ico, /h2-console/**, /** 모두 permitAll, H2 콘솔용: frameOptions sameOrigin(콘솔이 frame을 씀), /h2-console/** CSRF 검사 제외
- 이유: 회원가입·로그인을 만들기 위한 준비 (책 3-05, 14168 46강), 시큐리티를 넣으면 기본값으로 모든 요청이 로그인 필요 + POST/PUT/DELETE에 CSRF 토큰 검사 → 기존 화면이 막히지 않게 일단 전체 허용, th:action으로 만든 폼은 CSRF 토큰(_csrf)이 자동으로 들어가서 그대로 동작
- 결과: ./gradlew test 통과 (12개), 브라우저에서 로그인 없이 목록·상세 접근, 답변 폼에 _csrf hidden 자동 포함, 폼으로 답변 등록 성공, 토큰 없이 POST → 403, H2 콘솔 접속 확인

### 011-02 시큐리티, 정적 파일 위치 통일과 CSRF meta
- [링크](https://github.com/tmdghks-dev/sbb/commit/011-02)
- 작업: style.css를 static/resource/로 이동하고 레이아웃 경로 변경, SecurityConfig에 /resource/** permitAll, layout.html에 <meta name="_csrf" th:content="${_csrf.token}">
- 이유: 정적 파일을 한 폴더에 모아 두면 시큐리티 허용 규칙을 /resource/** 하나로 단순하게 유지할 수 있음 (14168 46강), JS로 만든 폼(015의 a[method] 삭제 링크 등)이 CSRF 토큰을 읽어 갈 수 있게 페이지마다 meta로 넣어 둠 (14168 46강)
- 결과: ./gradlew test 통과 (12개), 브라우저에서 /resource/style.css 200(예전 /style.css는 404), Pretendard 그대로 적용, meta _csrf에 토큰 값 확인

### 012-01 회원가입, Member 엔티티와 MemberRepository
- [링크](https://github.com/tmdghks-dev/sbb/commit/012-01)
- 작업: domain/member/member/entity/Member: BaseEntity 상속, username·email은 unique, password, setter 없이 생성자 Member(username, password, email), MemberRepository: findByUsername, findByEmail (Optional)
- 이유: 회원 정보를 저장하기 위해 (책 3-06 SiteUser), 이름은 SiteUser 대신 Member (설계 12), 아이디·이메일 중복을 DB 제약(unique)으로도 막기 위해, 책의 findByusername은 명명 오타 → findByUsername
- 결과: ./gradlew test 통과 (12개), SQL 로그에 create table member와 username·email unique 제약 생성 확인

### 012-02 회원가입, MemberService.join과 BCrypt 비밀번호 암호화
- [링크](https://github.com/tmdghks-dev/sbb/commit/012-02)
- 작업: SecurityConfig에 PasswordEncoder 빈(BCryptPasswordEncoder), MemberService: join(username, password, email)에서 비밀번호를 encode해서 저장, findByUsername, findByEmail, count, MemberServiceTest: 가입 시 비밀번호가 평문이 아닌 $2a$ 해시로 저장되고 matches로 확인되는지, 조회 메서드 동작
- 이유: 비밀번호를 평문으로 저장하면 DB가 유출될 때 그대로 노출되므로 단방향 해시(BCrypt)로 저장 (책 3-06), PasswordEncoder를 빈으로 등록해 서비스와 (013의) 로그인 처리가 같은 방식으로 비교하게, 테스트는 14163과 같은 형식(test 프로필 + @Transactional 롤백)
- 결과: ./gradlew test 통과 (14개)

### 012-03 회원가입, 가입 폼(GET·POST /members/join)과 검증
- [링크](https://github.com/tmdghks-dev/sbb/commit/012-03)
- 작업: MemberController: 중첩 record JoinForm(username 3~25자, password1, password2, email 형식) + @Valid/BindingResult, 비밀번호 확인이 다르면 password2에 오류, 아이디·이메일이 이미 있으면 각 필드에 오류 (rejectValue, 메시지는 순번-필드명-메시지 형식), templates/member/member/join.html (form_errors fragment 재사용), navbar에 회원가입 링크
- 이유: 책 3-06의 회원가입을 14168 폼 방식(같은 URL GET·POST, record 폼, 오류 정렬·focus)으로 구현, URL은 /members/join (설계 11), 책은 저장 중 DataIntegrityViolationException을 catch → 이 프로젝트는 컨트롤러에 트랜잭션이 있어 그 예외가 나면 트랜잭션이 롤백 전용이 되므로, 저장 전에 미리 조회해서 중복을 막음 (unique 제약은 마지막 안전장치)
- 결과: ./gradlew test 통과 (14개), 브라우저에서 비밀번호 확인 불일치 → 오류 표시 + 비밀번호 확인 칸 focus, 정상 가입 → 홈(질문 목록)으로 이동, 같은 아이디로 가입 → "이미 사용중인 사용자ID입니다.", 같은 이메일 → "이미 사용중인 이메일입니다." 확인

### 013-01 로그인, MemberRole·MemberSecurityService·formLogin
- [링크](https://github.com/tmdghks-dev/sbb/commit/013-01)
- 작업: MemberRole enum(ADMIN=ROLE_ADMIN, USER=ROLE_USER), MemberSecurityService implements UserDetailsService: username으로 회원 조회, admin이면 ROLE_ADMIN 아니면 ROLE_USER 권한, SecurityConfig에 formLogin(loginPage /members/login, 성공 시 /), GET /members/login 로그인 폼 화면, 실패 시(?error) 안내 문구, navbar에 로그인 링크, BaseInitData에 개발·테스트용 회원 admin, user1, user2 (비밀번호 1234, 아이디별로 없을 때만 생성)
- 이유: 로그인은 시큐리티가 처리: POST /members/login을 받으면 UserDetailsService로 회원을 찾고 PasswordEncoder로 비밀번호를 비교 (책 3-07), 로그인 폼 URL은 /members/login (설계 11), 화면 확인과 다음 기능(글쓴이·권한) 테스트를 위해 회원 시드 데이터 준비 (14168 BaseInitData 방식), 처음엔 "회원이 0명일 때만 생성"으로 했다가 012에서 가입 테스트한 회원 때문에 시드가 안 만들어지는 문제를 확인하고 아이디별 확인으로 변경
- 결과: ./gradlew test 통과 (14개), 브라우저에서 틀린 비밀번호 → /members/login?error와 안내 문구, user1/1234 로그인 → 홈(질문 목록)으로 이동 확인

### 013-02 로그아웃 준비, global.js (a[method] → 폼 전송)
- [링크](https://github.com/tmdghks-dev/sbb/commit/013-02)
- 작업: static/resource/global.js 추가 (14168 그대로): method 속성이 있는 a 태그를 클릭하면 POST 폼을 만들어 _csrf(meta에서 읽음)와 _method를 담아 전송, onclick(confirm 등)이 false면 중단, select[value] 기본값 지정, layout.html에서 모든 페이지에 defer로 로드
- 이유: a 태그(링크)는 GET만 보낼 수 있어서, 로그아웃·삭제처럼 상태를 바꾸는 요청을 링크 모양 그대로 POST/DELETE로 보내기 위해 (14168 42~44강), 책은 삭제를 GET 링크 + JS location.href로 처리해서 CSRF 보호를 받지 못함, 011-02에서 넣은 meta _csrf를 여기서 사용
- 결과: ./gradlew test 통과 (14개), 브라우저에서 /resource/global.js 200, 모든 페이지에 script 포함 확인 (실제 동작은 013-03 로그아웃에서 확인)

### 013-03 로그아웃, POST 로그아웃과 navbar 로그인 상태 표시
- [링크](https://github.com/tmdghks-dev/sbb/commit/013-03)
- 작업: thymeleaf-extras-springsecurity6 의존성 추가 (버전은 Boot BOM 관리), SecurityConfig에 logout(logoutUrl /members/logout, 성공 시 /, 세션 무효화), navbar: 비로그인(sec:authorize="isAnonymous()")이면 로그인·회원가입, 로그인 상태면 "아이디님"과 로그아웃 링크 <a method="POST" href="/members/logout">
- 이유: Spring Security 7은 CSRF가 켜져 있으면 로그아웃을 POST로만 받음 → 책의 GET 로그아웃(AntPathRequestMatcher)은 그대로 쓸 수 없음 (설계 17), 링크 모양은 그대로 두고 013-02의 global.js가 CSRF 토큰을 담은 POST 폼으로 바꿔 전송 (14168 a[method]), 로그인 여부에 따라 메뉴를 다르게 보여주기 위해 sec:authorize 사용 (책 3-07)
- 결과: ./gradlew test 통과 (14개), 브라우저에서 비로그인 navbar: 로그인/회원가입, user1 로그인 후: user1님/로그아웃, GET /members/logout은 404이고 로그인 유지, 로그아웃 링크 클릭(POST) → 로그아웃되어 다시 로그인/회원가입 표시 확인

### 014-01 글쓴이, 로그인한 사람만 질문·답변 작성 (@EnableMethodSecurity + @PreAuthorize)
- [링크](https://github.com/tmdghks-dev/sbb/commit/014-01)
- 작업: SecurityConfig에 @EnableMethodSecurity, 질문 등록 폼·처리, 답변 등록 액션에 @PreAuthorize("isAuthenticated()")
- 이유: 글쓴이를 저장하려면 작성할 때 로그인 사용자가 있어야 함 (책 3-08), @EnableMethodSecurity가 없으면 @PreAuthorize가 오류 없이 조용히 무시되므로 반드시 함께 추가 (책 v3.08, 계획 검증에서 발견), 책은 글쓴이 필드를 먼저 넣지만, 그러면 비로그인 작성 시 오류가 나는 중간 상태가 생겨서 로그인 확인을 먼저 적용
- 결과: ./gradlew test 통과 (14개), 브라우저에서 비로그인으로 /questions/write → 로그인 페이지로 이동, 비로그인 답변 등록 → 로그인 페이지로 이동, user1 로그인 후 질문 등록 폼 정상 표시 확인
