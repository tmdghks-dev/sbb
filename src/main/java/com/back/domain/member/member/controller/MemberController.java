package com.back.domain.member.member.controller;

import com.back.domain.member.member.service.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    public record JoinForm(
            @NotBlank(message = "01-username-사용자ID는 필수항목입니다.")
            @Size(min = 3, max = 25, message = "02-username-사용자ID는 3자 이상, 25자 이하로 입력가능합니다.")
            String username,
            @NotBlank(message = "03-password1-비밀번호는 필수항목입니다.")
            String password1,
            @NotBlank(message = "04-password2-비밀번호 확인은 필수항목입니다.")
            String password2,
            @NotBlank(message = "05-email-이메일은 필수항목입니다.")
            @Email(message = "06-email-이메일 형식이 올바르지 않습니다.")
            String email
    ) {
    }

    // 로그인 처리(POST /members/login)는 스프링 시큐리티가 한다. 여기서는 폼만 보여준다.
    @GetMapping("/members/login")
    public String showLogin() {
        return "member/member/login";
    }

    @GetMapping("/members/join")
    public String showJoin(@ModelAttribute("form") JoinForm form) {
        return "member/member/join";
    }

    @PostMapping("/members/join")
    @Transactional
    public String join(
            @ModelAttribute("form") @Valid JoinForm form,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) return "member/member/join";

        if (!form.password1.equals(form.password2)) {
            bindingResult.rejectValue("password2", "passwordInCorrect",
                    "07-password2-2개의 비밀번호가 일치하지 않습니다.");
            return "member/member/join";
        }

        // 책은 저장하다가 난 DB 오류(DataIntegrityViolationException)를 catch 한다.
        // 이 프로젝트는 트랜잭션이 컨트롤러 액션에 걸려 있어, 그 오류가 나면 트랜잭션이 롤백 전용이 되어 버린다.
        // 그래서 저장 전에 미리 조회해서 중복을 막는다. (DB의 unique 제약은 마지막 안전장치)
        if (memberService.findByUsername(form.username).isPresent()) {
            bindingResult.rejectValue("username", "duplicate",
                    "08-username-이미 사용중인 사용자ID입니다.");
            return "member/member/join";
        }

        if (memberService.findByEmail(form.email).isPresent()) {
            bindingResult.rejectValue("email", "duplicate",
                    "09-email-이미 사용중인 이메일입니다.");
            return "member/member/join";
        }

        memberService.join(form.username, form.password1, form.email);

        return "redirect:/";
    }
}
