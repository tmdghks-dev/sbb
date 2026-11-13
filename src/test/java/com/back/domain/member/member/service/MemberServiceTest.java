package com.back.domain.member.member.service;

import com.back.domain.member.member.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class MemberServiceTest {
    @Autowired
    private MemberService memberService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원가입: 비밀번호는 BCrypt 해시로 저장된다")
    void t1() {
        Member member = memberService.join("newUser", "1234", "newUser@test.com");

        assertThat(member.getId()).isGreaterThan(0);
        assertThat(member.getPassword()).isNotEqualTo("1234");
        assertThat(member.getPassword()).startsWith("$2a$");
        assertThat(passwordEncoder.matches("1234", member.getPassword())).isTrue();
    }

    @Test
    @DisplayName("findByUsername, findByEmail")
    void t2() {
        memberService.join("newUser", "1234", "newUser@test.com");

        assertThat(memberService.findByUsername("newUser")).isPresent();
        assertThat(memberService.findByEmail("newUser@test.com")).isPresent();
        assertThat(memberService.findByUsername("nobody")).isEmpty();
    }
}
