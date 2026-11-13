package com.back.domain.question.answer.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.question.answer.entity.Answer;
import com.back.domain.question.question.entity.Question;
import com.back.domain.question.question.service.QuestionService;
import com.back.global.exception.DataNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class AnswerController {
    private final QuestionService questionService;
    private final MemberService memberService;

    public record WriteForm(
            @NotBlank
            String content
    ) {
    }

    @PostMapping("/questions/{questionId}/answers/write")
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public String write(
            @PathVariable int questionId,
            @Valid WriteForm writeForm,
            Principal principal
    ) {
        Question question = findQuestion(questionId);
        // save 를 호출하지 않는다. 트랜잭션이 끝날 때 cascade PERSIST 로 INSERT 된다.
        Member author = memberService.findByUsername(principal.getName()).get();
        questionService.writeAnswer(question, author, writeForm.content);
        return "redirect:/questions/" + questionId;
    }

    public record ModifyForm(
            @NotBlank
            String content
    ) {
    }

    @GetMapping("/questions/{questionId}/answers/{id}/modify")
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public String showModify(
            @PathVariable int questionId,
            @PathVariable int id,
            Principal principal,
            Model model
    ) {
        Question question = findQuestion(questionId);
        Answer answer = findAnswerCheckAuthor(question, id, principal, "수정");
        model.addAttribute("question", question);
        model.addAttribute("answer", answer);
        return "question/answer/modify";
    }

    @PutMapping("/questions/{questionId}/answers/{id}/modify")
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public String modify(
            @PathVariable int questionId,
            @PathVariable int id,
            @Valid ModifyForm modifyForm,
            Principal principal
    ) {
        Question question = findQuestion(questionId);
        Answer answer = findAnswerCheckAuthor(question, id, principal, "수정");
        // save 없이 dirty checking 으로 UPDATE 된다.
        questionService.modifyAnswer(answer, modifyForm.content);
        return "redirect:/questions/" + questionId;
    }

    @DeleteMapping("/questions/{questionId}/answers/{id}/delete")
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public String delete(
            @PathVariable int questionId,
            @PathVariable int id,
            Principal principal
    ) {
        Question question = findQuestion(questionId);
        Answer answer = findAnswerCheckAuthor(question, id, principal, "삭제");
        questionService.deleteAnswer(question, answer);
        return "redirect:/questions/" + questionId;
    }

    @PostMapping("/questions/{questionId}/answers/{id}/vote")
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public String vote(
            @PathVariable int questionId,
            @PathVariable int id,
            Principal principal
    ) {
        Question question = findQuestion(questionId);
        Answer answer = question.findAnswerById(id)
                .orElseThrow(() -> new DataNotFoundException("%d번 답변이 없습니다.".formatted(id)));
        Member voter = memberService.findByUsername(principal.getName()).get();
        questionService.voteAnswer(answer, voter);
        return "redirect:/questions/" + questionId;
    }

    private Question findQuestion(int questionId) {
        return questionService.findById(questionId)
                .orElseThrow(() -> new DataNotFoundException("%d번 질문이 없습니다.".formatted(questionId)));
    }

    // 답변은 질문을 통해서만 찾는다. (AnswerRepository 없음) 로그인한 사람이 작성자가 아니면 거절한다.
    private Answer findAnswerCheckAuthor(Question question, int id, Principal principal, String actionName) {
        Answer answer = question.findAnswerById(id)
                .orElseThrow(() -> new DataNotFoundException("%d번 답변이 없습니다.".formatted(id)));
        Member actor = memberService.findByUsername(principal.getName()).get();

        if (!answer.isAuthor(actor)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "%s권한이 없습니다.".formatted(actionName));
        }

        return answer;
    }
}
