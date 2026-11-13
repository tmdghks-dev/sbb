package com.back.domain.question.answer.controller;

import com.back.domain.question.question.entity.Question;
import com.back.domain.question.question.service.QuestionService;
import com.back.global.exception.DataNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AnswerController {
    private final QuestionService questionService;

    public record WriteForm(
            @NotBlank
            String content
    ) {
    }

    @PostMapping("/questions/{questionId}/answers/write")
    @Transactional
    public String write(
            @PathVariable int questionId,
            @Valid WriteForm writeForm
    ) {
        Question question = questionService.findById(questionId)
                .orElseThrow(() -> new DataNotFoundException("%d번 질문이 없습니다.".formatted(questionId)));
        // save 를 호출하지 않는다. 트랜잭션이 끝날 때 cascade PERSIST 로 INSERT 된다.
        questionService.writeAnswer(question, writeForm.content);
        return "redirect:/questions/" + questionId;
    }
}
