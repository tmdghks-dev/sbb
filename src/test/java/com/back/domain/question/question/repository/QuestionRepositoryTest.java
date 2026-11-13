package com.back.domain.question.question.repository;

import com.back.domain.question.answer.entity.Answer;
import com.back.domain.question.question.entity.Question;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class QuestionRepositoryTest {
    @Autowired
    private QuestionRepository questionRepository;

    @Test
    @DisplayName("findAll")
    void t1() {
        List<Question> questions = questionRepository.findAll();

        assertThat(questions).hasSize(2);

        Question question = questions.get(0);
        assertThat(question.getSubject()).isEqualTo("sbb가 무엇인가요?");
    }

    @Test
    @DisplayName("findById")
    void t2() {
        Question question = questionRepository.findById(1).get();
        assertThat(question.getSubject()).isEqualTo("sbb가 무엇인가요?");
    }

    @Test
    @DisplayName("findBySubject")
    void t3() {
        Question question = questionRepository.findBySubject("sbb가 무엇인가요?").get();
        assertThat(question.getId()).isEqualTo(1);
    }

    @Test
    @DisplayName("findBySubjectAndContent")
    void t4() {
        Question question = questionRepository.findBySubjectAndContent("sbb가 무엇인가요?", "sbb에 대해서 알고 싶습니다.").get();
        assertThat(question.getId()).isEqualTo(1);
    }

    @Test
    @DisplayName("findBySubjectLike")
    void t5() {
        List<Question> questions = questionRepository.findBySubjectLike("sbb%");

        Question question = questions.get(0);
        assertThat(question.getSubject()).isEqualTo("sbb가 무엇인가요?");
    }

    @Test
    @DisplayName("수정 (save 없이 dirty checking)")
    void t6() {
        Question question = questionRepository.findById(1).get();

        question.modify("수정된 제목", "수정된 내용");
        // save 를 호출하지 않아도 트랜잭션 안에서 바뀐 값은 UPDATE 된다.
        // 아래 JPQL 조회 직전에 변경 내용이 자동으로 flush 된다.

        Question foundQuestion = questionRepository.findBySubject("수정된 제목").get();
        assertThat(foundQuestion.getId()).isEqualTo(1);
    }

    @Test
    @DisplayName("t6 의 수정은 롤백되어 다른 테스트에 영향을 주지 않는다")
    void t7() {
        assertThat(questionRepository.findBySubject("수정된 제목")).isEmpty();
        assertThat(questionRepository.findBySubject("sbb가 무엇인가요?")).isPresent();
    }

    @Test
    @DisplayName("삭제")
    void t8() {
        assertThat(questionRepository.count()).isEqualTo(2);

        Question question = questionRepository.findById(1).get();
        questionRepository.delete(question);

        assertThat(questionRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("답변 생성 by oneToMany (addAnswer)")
    void t9() {
        Question question = questionRepository.findById(2).get();

        int beforeCount = question.getAnswers().size();

        Answer newAnswer = question.addAnswer("네 자동으로 생성됩니다.");

        // 아직 INSERT 가 실행되지 않았으므로(flush 전) id 는 0 이다.
        assertThat(newAnswer.getId()).isEqualTo(0);

        int afterCount = question.getAnswers().size();
        assertThat(afterCount).isEqualTo(beforeCount + 1);

        // flush 하면 cascade PERSIST 로 INSERT 되어 id 가 생긴다.
        questionRepository.flush();
        assertThat(newAnswer.getId()).isGreaterThan(0);
    }

    @Test
    @DisplayName("답변 조회 by oneToMany (LAZY 로딩)")
    void t10() {
        Question question = questionRepository.findById(2).get();

        // answers 는 이 시점에 SELECT 된다. 테스트의 @Transactional 덕분에 커넥션이 살아 있어 LAZY 로딩이 가능하다.
        List<Answer> answers = question.getAnswers();
        assertThat(answers).hasSize(2);

        Answer answer = answers.get(0);
        assertThat(answer.getContent()).isEqualTo("네 자동으로 생성됩니다.");
    }

    @Test
    @DisplayName("페이징 (최신순)")
    void t11() {
        Page<Question> page = questionRepository.findAll(
                PageRequest.of(0, 1, Sort.by(Sort.Order.desc("createDate")))
        );

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getSubject()).isEqualTo("스프링부트 모델 질문입니다.");
    }
}
