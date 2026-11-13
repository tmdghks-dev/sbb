package com.back.domain.question.question.service;

import com.back.domain.question.question.entity.Question;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class QuestionServiceTest {
    @Autowired
    private QuestionService questionService;

    @Test
    @DisplayName("검색어가 없으면 전체")
    void t1() {
        Page<Question> page = questionService.findPage(0, "");
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("제목으로 검색")
    void t2() {
        Page<Question> page = questionService.findPage(0, "sbb가");
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(1);
    }

    @Test
    @DisplayName("답변 내용으로 검색 (답변이 2개 걸려도 질문은 한 번만)")
    void t3() {
        Page<Question> page = questionService.findPage(0, "자동");
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(2);
    }

    @Test
    @DisplayName("작성자 아이디로 검색 (질문 작성자 user1)")
    void t4() {
        Page<Question> page = questionService.findPage(0, "user1");
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("답변 작성자 아이디로 검색 (답변 작성자 user2)")
    void t5() {
        Page<Question> page = questionService.findPage(0, "user2");
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(2);
    }
}
