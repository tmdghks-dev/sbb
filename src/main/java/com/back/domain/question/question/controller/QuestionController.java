package com.back.domain.question.question.controller;

import com.back.domain.question.question.entity.Question;
import com.back.domain.question.question.service.QuestionService;
import com.back.global.exception.DataNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
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
            String subject,
            String content
    ) {
    }

    @GetMapping("/questions/write")
    public String showWrite(@ModelAttribute("form") WriteForm form) {
        return "question/question/write";
    }

    @PostMapping("/questions/write")
    @Transactional
    public String write(@ModelAttribute("form") WriteForm form) {
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
