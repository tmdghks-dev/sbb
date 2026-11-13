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
}
