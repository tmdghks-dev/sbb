package com.back.domain.question.question.entity;

import com.back.domain.member.member.entity.Member;
import com.back.domain.question.answer.entity.Answer;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor
public class Question extends BaseEntity {
    @ManyToOne(fetch = LAZY)
    private Member author; // AUTHOR_ID

    @Column(length = 200)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String content;

    @OneToMany(mappedBy = "question", cascade = {PERSIST, REMOVE}, orphanRemoval = true)
    private final List<Answer> answers = new ArrayList<>();

    public Question(Member author, String subject, String content) {
        this.author = author;
        this.subject = subject;
        this.content = content;
    }

    // author 는 LAZY 로딩된 프록시일 수 있어서 equals 대신 id 로 비교한다. (BaseEntity.equals 는 클래스까지 비교)
    public boolean isAuthor(Member member) {
        if (author == null || member == null) return false;
        return author.getId() == member.getId();
    }

    public void modify(String subject, String content) {
        this.subject = subject;
        this.content = content;
    }

    public Answer addAnswer(Member author, String content) {
        Answer answer = new Answer(this, author, content);
        answers.add(answer);
        return answer;
    }

    public Optional<Answer> findAnswerById(int id) {
        return answers
                .stream()
                .filter(answer -> answer.getId() == id)
                .findFirst();
    }
}
