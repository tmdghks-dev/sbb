package com.back;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.filter.HiddenHttpMethodFilter;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class BackApplicationTests {
    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
    }

    @Test
    @DisplayName("_method 파라미터로 PUT/DELETE 를 쓰기 위한 HiddenHttpMethodFilter 가 등록되어 있다")
    void hiddenHttpMethodFilterEnabled() {
        assertThat(context.getBeansOfType(HiddenHttpMethodFilter.class)).isNotEmpty();
    }
}
