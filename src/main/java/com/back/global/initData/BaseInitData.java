package com.back.global.initData;

import com.back.domain.question.question.entity.Question;
import com.back.domain.question.question.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    // self.work1() 처럼 프록시를 거쳐 호출해야 @Transactional 이 적용된다.
    @Autowired
    @Lazy
    private BaseInitData self;

    private final QuestionService questionService;

    @Bean
    ApplicationRunner baseInitDataApplicationRunner() {
        return args -> {
            self.work1();
        };
    }

    @Transactional
    public void work1() {
        if (questionService.count() > 0) return;

        Question question1 = questionService.write("sbb가 무엇인가요?", "sbb에 대해서 알고 싶습니다.");
        Question question2 = questionService.write("스프링부트 모델 질문입니다.", "id는 자동으로 생성되나요?");

        question2.addAnswer("네 자동으로 생성됩니다.");
        question2.addAnswer("네, @GeneratedValue 로 자동 생성됩니다.");
    }
}
