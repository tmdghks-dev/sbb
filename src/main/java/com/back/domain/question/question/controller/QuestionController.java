package com.back.domain.question.question.controller;

import com.back.domain.question.question.entity.Question;
import com.back.domain.question.question.service.QuestionService;
import com.back.global.exception.DataNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;

    @GetMapping("/questions")
    @Transactional(readOnly = true)
    public String showList(Model model) {
        List<Question> questions = questionService.findAll();
        model.addAttribute("questions", questions);
        return "question/question/list";
    }

    public record WriteForm(
            @NotBlank(message = "01-subject-제목을 입력해주세요.")
            @Size(max = 200, message = "02-subject-제목은 200자 이하로 입력가능합니다.")
            String subject,
            @NotBlank(message = "03-content-내용을 입력해주세요.")
            String content
    ) {
    }

    @GetMapping("/questions/write")
    public String showWrite(@ModelAttribute("form") WriteForm form) {
        return "question/question/write";
    }

    @PostMapping("/questions/write")
    @Transactional
    public String write(
            @ModelAttribute("form") @Valid WriteForm form,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) return "question/question/write";

        Question question = questionService.write(form.subject, form.content);
        return "redirect:/questions/" + question.getId();
    }

    @GetMapping("/questions/{id}")
    @Transactional(readOnly = true)
    public String showDetail(
            @PathVariable int id,
            Model model
    ) {
        Question question = questionService.findById(id)
                .orElseThrow(() -> new DataNotFoundException("%d번 질문이 없습니다.".formatted(id)));
        model.addAttribute("question", question);
        return "question/question/detail";
    }

    @GetMapping("/questions/")
    public String redirectToList() {
        return "redirect:/questions";
    }
}
