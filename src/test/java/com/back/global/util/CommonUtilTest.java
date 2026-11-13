package com.back.global.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommonUtilTest {
    private final CommonUtil commonUtil = new CommonUtil();

    @Test
    @DisplayName("마크다운을 HTML 로 바꾼다")
    void t1() {
        String html = commonUtil.markdown("# 제목\n\n**굵게** 와 `코드`\n\n- 항목1\n- 항목2");

        assertThat(html).contains("<h1>제목</h1>");
        assertThat(html).contains("<strong>굵게</strong>");
        assertThat(html).contains("<code>코드</code>");
        assertThat(html).contains("<li>항목1</li>");
    }
}
