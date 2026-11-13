package com.back.domain.question.question.repository;

import com.back.domain.question.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
    // SELECT * FROM question WHERE subject = ?
    Optional<Question> findBySubject(String subject);

    // SELECT * FROM question WHERE subject = ? AND content = ?
    Optional<Question> findBySubjectAndContent(String subject, String content);

    // SELECT * FROM question WHERE subject LIKE ?
    List<Question> findBySubjectLike(String subject);
}
