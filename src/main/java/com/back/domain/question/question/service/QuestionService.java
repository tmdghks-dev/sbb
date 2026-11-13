package com.back.domain.question.question.service;

import com.back.domain.question.question.entity.Question;
import com.back.domain.question.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestionService {
    private final QuestionRepository questionRepository;

    public long count() {
        return questionRepository.count();
    }

    public Question write(String subject, String content) {
        Question question = new Question(subject, content);
        return questionRepository.save(question);
    }

    public Optional<Question> findById(int id) {
        return questionRepository.findById(id);
    }

    public List<Question> findAll() {
        return questionRepository.findAll();
    }
}
