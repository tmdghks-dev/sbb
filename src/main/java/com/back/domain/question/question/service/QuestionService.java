package com.back.domain.question.question.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.question.answer.entity.Answer;
import com.back.domain.question.question.entity.Question;
import com.back.domain.question.question.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public Question write(Member author, String subject, String content) {
        Question question = new Question(author, subject, content);
        return questionRepository.save(question);
    }

    public Optional<Question> findById(int id) {
        return questionRepository.findById(id);
    }

    public List<Question> findAll() {
        return questionRepository.findAll();
    }

    public Page<Question> findPage(int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Order.desc("createDate")));
        return questionRepository.findAll(pageable);
    }

    public void modify(Question question, String subject, String content) {
        // save 를 호출하지 않는다. 트랜잭션 안에서 바뀐 값은 dirty checking 으로 UPDATE 된다.
        question.modify(subject, content);
    }

    public void delete(Question question) {
        // cascade REMOVE 로 달린 답변도 함께 삭제된다.
        questionRepository.delete(question);
    }

    public Answer writeAnswer(Question question, Member author, String content) {
        Answer answer = question.addAnswer(author, content);
        // 답변 id 는 INSERT 가 실행되어야 생긴다. (IDENTITY 전략)
        // 작성 직후 그 답변 위치(#answer_id)로 이동하려면 id 가 필요하므로, 커밋 전에 flush 로 INSERT 를 먼저 실행한다.
        questionRepository.flush();
        return answer;
    }

    public void vote(Question question, Member voter) {
        question.vote(voter);
    }

    public void voteAnswer(Answer answer, Member voter) {
        answer.vote(voter);
    }

    public void modifyAnswer(Answer answer, String content) {
        answer.modify(content);
    }

    public boolean deleteAnswer(Question question, Answer answer) {
        return question.deleteAnswer(answer);
    }
}
