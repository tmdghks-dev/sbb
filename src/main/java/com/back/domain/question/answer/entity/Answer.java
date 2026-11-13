package com.back.domain.question.answer.entity;

import com.back.domain.member.member.entity.Member;
import com.back.domain.question.question.entity.Question;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor
public class Answer extends BaseEntity {
    @ManyToOne(fetch = LAZY)
    private Question question; // QUESTION_ID

    @ManyToOne(fetch = LAZY)
    private Member author; // AUTHOR_ID

    @Column(columnDefinition = "TEXT")
    private String content;

    public Answer(Question question, Member author, String content) {
        this.question = question;
        this.author = author;
        this.content = content;
    }

    // author 는 LAZY 로딩된 프록시일 수 있어서 equals 대신 id 로 비교한다.
    public boolean isAuthor(Member member) {
        if (author == null || member == null) return false;
        return author.getId() == member.getId();
    }

    public void modify(String content) {
        this.content = content;
    }
}
