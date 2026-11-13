package com.back.global.util;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Component;

// 템플릿에서 ${commonUtil.markdown(...)} 으로 호출한다. (컨트롤러가 모델에 넣어 줌)
@Component
public class CommonUtil {
    private final Parser parser = Parser.builder().build();

    // escapeHtml: 마크다운 안에 직접 쓴 HTML(<script> 등)을 태그가 아니라 글자로 출력
    // sanitizeUrls: javascript: 같은 위험한 링크 주소를 무력화
    private final HtmlRenderer renderer = HtmlRenderer.builder()
            .escapeHtml(true)
            .sanitizeUrls(true)
            .build();

    public String markdown(String markdown) {
        Node document = parser.parse(markdown);
        return renderer.render(document);
    }
}
