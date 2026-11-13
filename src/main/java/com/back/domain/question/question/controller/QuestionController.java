package com.back.domain.question.question.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.question.question.entity.Question;
import com.back.domain.question.question.service.QuestionService;
import com.back.global.exception.DataNotFoundException;
import com.back.global.util.CommonUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;
    private final MemberService memberService;
    private final CommonUtil commonUtil;

    @GetMapping("/questions")
    @Transactional(readOnly = true)
    public String showList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String kw,
            Model model
    ) {
        Page<Question> paging = questionService.findPage(page, kw);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
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
    @PreAuthorize("isAuthenticated()")
    public String showWrite(@ModelAttribute("form") WriteForm form) {
        return "question/question/write";
    }

    @PostMapping("/questions/write")
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public String write(
            @ModelAttribute("form") @Valid WriteForm form,
            BindingResult bindingResult,
            Principal principal
    ) {
        if (bindingResult.hasErrors()) return "question/question/write";

        Member author = memberService.findByUsername(principal.getName()).get();
        Question question = questionService.write(author, form.subject, form.content);
        return "redirect:/questions/" + question.getId();
    }

    public record ModifyForm(
            @NotBlank(message = "01-subject-제목을 입력해주세요.")
            @Size(max = 200, message = "02-subject-제목은 200자 이하로 입력가능합니다.")
            String subject,
            @NotBlank(message = "03-content-내용을 입력해주세요.")
            String content
    ) {
    }

    @GetMapping("/questions/{id}/modify")
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public String showModify(
            @PathVariable int id,
            Principal principal,
            Model model
    ) {
        Question question = findQuestionCheckAuthor(id, principal, "수정");
        model.addAttribute("question", question);
        model.addAttribute("form", new ModifyForm(question.getSubject(), question.getContent()));
        return "question/question/modify";
    }

    @PutMapping("/questions/{id}/modify")
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public String modify(
            @PathVariable int id,
            @ModelAttribute("form") @Valid ModifyForm form,
            BindingResult bindingResult,
            Principal principal,
            Model model
    ) {
        Question question = findQuestionCheckAuthor(id, principal, "수정");
        model.addAttribute("question", question);

        if (bindingResult.hasErrors()) return "question/question/modify";

        questionService.modify(question, form.subject, form.content);
        return "redirect:/questions/" + question.getId();
    }

    @DeleteMapping("/questions/{id}/delete")
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public String delete(
            @PathVariable int id,
            Principal principal
    ) {
        Question question = findQuestionCheckAuthor(id, principal, "삭제");
        questionService.delete(question);
        return "redirect:/";
    }

    // 추천은 데이터를 바꾸는 요청이라 GET 이 아니라 POST 로 받는다.
    @PostMapping("/questions/{id}/vote")
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public String vote(
            @PathVariable int id,
            Principal principal
    ) {
        Question question = questionService.findById(id)
                .orElseThrow(() -> new DataNotFoundException("%d번 질문이 없습니다.".formatted(id)));
        Member voter = memberService.findByUsername(principal.getName()).get();
        questionService.vote(question, voter);
        return "redirect:/questions/" + id;
    }

    // 질문을 찾고, 로그인한 사람이 작성자가 아니면 거절한다.
    private Question findQuestionCheckAuthor(int id, Principal principal, String actionName) {
        Question question = questionService.findById(id)
                .orElseThrow(() -> new DataNotFoundException("%d번 질문이 없습니다.".formatted(id)));
        Member actor = memberService.findByUsername(principal.getName()).get();

        if (!question.isAuthor(actor)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "%s권한이 없습니다.".formatted(actionName));
        }

        return question;
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
        // 최신 Thymeleaf 는 th:utext 안에서 ${@빈이름...} 호출을 막으므로, 모델로 넘겨서 ${commonUtil...} 로 쓴다.
        model.addAttribute("commonUtil", commonUtil);
        return "question/question/detail";
    }

    @GetMapping("/questions/")
    public String redirectToList() {
        return "redirect:/questions";
    }
}
