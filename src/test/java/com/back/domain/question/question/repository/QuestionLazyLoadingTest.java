package com.back.domain.question.question.repository;

import com.back.domain.question.question.entity.Question;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

// [실험] 일부러 @Transactional 을 붙이지 않은 테스트
@ActiveProfiles("test")
@SpringBootTest
class QuestionLazyLoadingTest {
    @Autowired
    private QuestionRepository questionRepository;

    @Test
    @DisplayName("[실험] 트랜잭션 밖에서는 LAZY 컬렉션을 읽을 수 없다")
    void t1() {
        // findById 는 자기만의 짧은 트랜잭션에서 실행되고 끝나면 영속성 컨텍스트가 닫힌다.
        Question question = questionRepository.findById(2).get();

        // answers 는 LAZY 라서 아직 조회되지 않았고, 이제 DB 커넥션이 없으므로 조회에 실패한다.
        // (QuestionRepositoryTest 는 클래스에 @Transactional 이 있어서 t10 처럼 같은 코드가 통과한다)
        assertThatThrownBy(() -> question.getAnswers().size())
                .isInstanceOf(LazyInitializationException.class);
    }
}
