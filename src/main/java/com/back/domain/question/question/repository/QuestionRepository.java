package com.back.domain.question.question.repository;

import com.back.domain.question.question.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
    // SELECT * FROM question WHERE subject = ?
    Optional<Question> findBySubject(String subject);

    // SELECT * FROM question WHERE subject = ? AND content = ?
    Optional<Question> findBySubjectAndContent(String subject, String content);

    // SELECT * FROM question WHERE subject LIKE ?
    List<Question> findBySubjectLike(String subject);

    // 제목, 내용, 질문 작성자, 답변 내용, 답변 작성자 중 하나라도 kw 를 포함하는 질문 (중복 제거)
    @Query("""
            select distinct q
            from Question q
            left outer join q.author u1
            left outer join q.answers a
            left outer join a.author u2
            where q.subject like %:kw%
               or q.content like %:kw%
               or u1.username like %:kw%
               or a.content like %:kw%
               or u2.username like %:kw%
            """)
    Page<Question> findAllByKeyword(@Param("kw") String kw, Pageable pageable);
}
